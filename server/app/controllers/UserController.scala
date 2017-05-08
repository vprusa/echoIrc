package controllers

import java.io.File
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import models.Logs
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import service.MyEnvironment
import shared.Shared
import models.Logs

import utils.IrcLogBot

import scala.collection.mutable.ListBuffer

// http://stackoverflow.com/questions/37371698/could-not-find-implicit-value-for-parameter-messages-play-api-i18n-messages-in

/**
  * A very simple chat client using websockets.
  */
@Singleton
class UserController @Inject()(
                                implicit val actorSystem: ActorSystem,
                                override implicit val env: MyEnvironment,
                                override implicit val webJarAssets: WebJarAssets,
                                override implicit val messagesApi: MessagesApi
                              )
  extends BaseController()(env, webJarAssets, messagesApi) with I18nSupport {

  def getLogsNames = SecuredAction { request =>
    //val body: AnyContent = request.body

    val logs: Logs = new Logs(request.user.main.userId)

    val files: List[File] = logs.getLogsFiles()

    var fileNames: ListBuffer[String] = ListBuffer.empty[String]

    files.foreach(f => {
      fileNames += f.getName
    })

    Ok(upickle.default.write(fileNames))
  }

  def getLogFile(filename: String) = SecuredAction { request =>
    val logs: Logs = new Logs(request.user.main.userId)
    Ok.sendFile(new java.io.File(logs.USER_LOG_JS_DIR + s"/${filename}"))
  }

  def getPureLogFile(filename: String) = SecuredAction { request =>
    val logs: Logs = new Logs(request.user.main.userId)
    logs.createSimpleIfNotExist()
    Ok.sendFile(new java.io.File(logs.USER_LOG_SIMPLE_DIR + s"/${filename}"))
  }


  def getParticipants(target: String) = SecuredAction { request =>
    // request.user.main.userId
    Logger.debug("getParticipants")
    val bot: IrcLogBot = Shared.ircLogBotMap.getOrElse(request.user.main.userId, null)
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
    Ok("")
  }

}

