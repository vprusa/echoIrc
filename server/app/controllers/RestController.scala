package controllers

import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import models.WebsocketUser
import models.WebsocketUser._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, _}
import play.api.mvc._
import securesocial.core.SecureSocial
import service.{DemoUser, MyEnvironment}
import shared.Shared
import utils.IrcLogBot

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

  def rotateLogsNow = SecuredAction { request =>
    Logger.debug("rotateLogsNow")
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    // Expecting json body
    jsonBody.map { json =>
      Logger.debug("rotateLogsNow.jsonBody.map ")

      val ircBot: IrcLogBot = Shared.ircLogBotMap.getOrElse(request.user.main.userId, null)

      if (ircBot != null) {
        // createnew Log instance creates new log file
        ircBot.defaultListener.logs.rotateNow()
      }

      Ok("Got: " + (json \ "name").as[String])
    }.getOrElse {
      BadRequest("Expecting application/json request body")
    }
  }

  //  def getUsersLogPart(fromDateTime: String, toDateTime: String, fromLine: Int, linesCount: Int) = SecuredAction { implicit request =>

}
