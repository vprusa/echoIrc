package controllers

import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import models.{LogWrapper, WebsocketUser, LogsBase}
import models.WebsocketUser._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, _}
import play.api.mvc._
import securesocial.core.SecureSocial
import service.{DemoUser, MyEnvironment}
import shared.Shared
import shared.SharedMessages._
import utils.IrcLogBot

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, _}
import scala.util.Failure

// http://stackoverflow.com/questions/37371698/could-not-find-implicit-value-for-parameter-messages-play-api-i18n-messages-in

/**
  * A very simple chat client using websockets.
  */
@Singleton
class RestController @Inject()(
                                implicit val actorSystem: ActorSystem,
                                override implicit val env: MyEnvironment,
                                override implicit val webJarAssets: WebJarAssets,
                                override implicit val messagesApi: MessagesApi
                              )
  extends BaseController()(env, webJarAssets, messagesApi) with I18nSupport {

  def getLogSnippet = SecuredAction { request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    // TODO json needs to contian
    // from & to: DateTime
    // Expecting json body
    jsonBody.map { json =>
      Ok("Got: " + (json \ "name").as[String])
    }.getOrElse {
      BadRequest("Expecting application/json request body")
    }
  }

  def rotateLogsNow(target: String) = SecuredAction { request =>
    Logger.debug("rotateLogsNow")
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    // Expecting json body
    jsonBody.map { json =>
      Logger.debug("rotateLogsNow.jsonBody.map ")

      val ircBot: IrcLogBot = Shared.ircLogBotMap.getOrElse((request.user.main.userId, request.user.main.providerId), null)

      if (ircBot != null) {
        // createnew Log instance creates new log file
        ircBot.defaultListener.getCurrentLog(target).rotateNow()
        Ok("Got: " + (json \ "name").as[String])

      }
      Ok("Nothing to rotate")

    }.getOrElse {
      BadRequest("Expecting application/json request body")
    }
  }

  //  def getUsersLogPart(fromDateTime: String, toDateTime: String, fromLine: Int, linesCount: Int) = SecuredAction { implicit request =>

  def getLogsNames(target: String) = SecuredAction { request =>
    //val body: AnyContent = request.body

    val logs: LogWrapper = new LogWrapper((request.user.main.userId, request.user.main.providerId), target)

    val files: List[File] = logs.getLogsFiles()

    var fileNames: ListBuffer[String] = ListBuffer.empty[String]

    files.foreach(f => {
      fileNames += f.getName
    })

    Ok(upickle.default.write(fileNames))
  }

  def getAllLogsNames = SecuredAction { request =>
    //val body: AnyContent = request.body

    val logs: LogsBase = new LogsBase((request.user.main.userId, request.user.main.providerId))

    val jsmsg: JsMessageGetLogsNamesResponse = logs.getLogsAllTargetsFilesNames()

    Ok(upickle.default.write(jsmsg))
  }

  def getLogFile(target: String, filename: String) = SecuredAction { request =>
    val logs: LogWrapper = new LogWrapper((request.user.main.userId, request.user.main.providerId), target.replaceAll("%23", "#"))
    Ok.sendFile(new java.io.File(logs.USER_LOG_JS_DIR + s"/${filename}"))
  }

  /*
  //def getLogFile(target: String, filename: String) = SecuredAction { request =>
  def getLogFile = SecuredAction { request =>
    Logger.debug("getLogFile")
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson
    Logger.debug("headers")
    Logger.debug(request.headers.toString)
    Logger.debug(request.headers.toMap.toString)
    Logger.debug(request.headers.toSimpleMap.toString)
    Logger.debug("body")
    Logger.debug(request.body.toString)
    // Expecting json body
    Logger.debug("jsonBody.map")
    jsonBody.map { json =>
      val jsmsg: JsMessageGetLogRequest = upickle.default.read[JsMessageGetLogRequest](json.toString)
      val logs: LogWrapper = new LogWrapper((request.user.main.userId, request.user.main.providerId), jsmsg.target)
      Ok.sendFile(new java.io.File(logs.USER_LOG_JS_DIR + s"/${jsmsg.filename}"))
    }.getOrElse {
      BadRequest("Expecting application/json request body")
    }
  }
  */

  def getPureLogFile(filename: String, target: String) = SecuredAction { request =>
    val logs: LogWrapper = new LogWrapper((request.user.main.userId, request.user.main.providerId), target)
    logs.createSimpleIfNotExist()
    Ok.sendFile(new java.io.File(logs.USER_LOG_SIMPLE_DIR + s"/${filename}"))
  }


  def getParticipants(target: String) = SecuredAction { request =>
    // request.user.main.userId
    Logger.debug("getParticipants")
    val bot: IrcLogBot = Shared.ircLogBotMap.getOrElse((request.user.main.userId, request.user.main.providerId), null)
    if (bot != null) {
      var participantsNames: ListBuffer[String] = ListBuffer.empty[String]

      Logger.debug("bot not null")
      val iterator = bot.getUserBot.getChannels.iterator()

      while (iterator.hasNext) {
        val channel = iterator.next()
        Logger.debug(s"channel: ${channel.getName}")
        if (channel.getName.matches(target)) {

          var channelIterator = channel.getUsers.iterator()
          while (channelIterator.hasNext) {
            val user = channelIterator.next
            participantsNames += user.toString
          }
        }
      }
      Ok(upickle.default.write(participantsNames))
    }
    Ok("[]")
  }

  //  def searchLogs(regex: String) = SecuredAction { request =>
  def searchLogs = SecuredAction { request =>
    Logger.debug("searchLogs")
    Logger.debug(request.toString)
    Logger.debug("request.headers..")
    Logger.debug(request.headers.toString)
    val body: AnyContent = request.body
    Logger.debug("request.body")
    Logger.debug(request.body.toString)
    val jsonBody: Option[JsValue] = body.asJson
    Logger.debug("request.body.asJson")
    Logger.debug(request.body.asJson.toString)

    /*
    // Expecting json body
    jsonBody.map { json =>
      Logger.debug("searchLogs.jsonBody.map")
      val jsmsg: JsMessageSearchLogsRequest = upickle.default.read[JsMessageSearchLogsRequest](json.toString)
      val logs: LogsBase = new LogsBase((request.user.main.userId, request.user.main.providerId))

      Ok(logs.searchLogs(jsmsg).toString)
    }.getOrElse {
      BadRequest("Expecting application/json request body")
    }*/
    Ok("")
  }


  //  def searchLogs(regex: String) = SecuredAction { request =>
  def test = SecuredAction { request =>
    Logger.debug("test SecuredAction")
    Ok("test SecuredAction")
  }


}
