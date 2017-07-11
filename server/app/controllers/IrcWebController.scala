package controllers

import java.net.URL
import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import models.WebsocketUser
import models.WebsocketUser._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import securesocial.core.SecureSocial
import service.{DemoUser, MyEnvironment}
import shared.Shared
import utils.IrcLogBot

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, _}
import scala.xml.dtd.ContentModel

// http://stackoverflow.com/questions/37371698/could-not-find-implicit-value-for-parameter-messages-play-api-i18n-messages-in

/**
  * A very simple chat client using websockets.
  */
@Singleton
class IrcWebController @Inject()(
                                  implicit val actorSystem: ActorSystem,
                                  override implicit val env: MyEnvironment,
                                  override implicit val webJarAssets: WebJarAssets,
                                  override implicit val messagesApi: MessagesApi
                                )
  extends BaseController()(env, webJarAssets, messagesApi) with I18nSupport {

  def ircChat = SecuredAction { implicit request =>
    Ok(views.html.ircChat("url"))
  }

  def unpackUser(demoUserFutOpt: Future[Option[MyEnvironment#U]]): DemoUser = {
    var demoUserVar: DemoUser = null //TODO ..hate to use null but should be safe cause of Exception below
    Logger.debug("unpackUser")

    val maybeCurUser = for {
      f1Result <- demoUserFutOpt
    } yield (f1Result)

    maybeCurUser onComplete {
      case f => {}
    }
    maybeCurUser // this will make it wait for result
    if (maybeCurUser == None) {
      Logger.debug(s"maybeCurUser1 None")
      return null
    }
    import scala.concurrent.duration.Duration

    val result = Await.result(maybeCurUser, Duration.Inf)
    val someUser: Some[DemoUser] = result.asInstanceOf[Some[DemoUser]]

    someUser match {
      case Some(d: DemoUser) => {
        demoUserVar = d
      }
      case other => {
        // TODO SecurityException because no DemoUser found so probably forgery
        Logger.debug(s"SecurityException ${someUser.toString}")
        throw new Exception("SecurityException")
      }
    }

    if (demoUserVar == null) {
      throw new NullPointerException("NullPointerException")
    }
    demoUserVar
  }

  def getIrcBotByUser(demoUser: DemoUser): IrcLogBot = {
    if (demoUser == null) {
      null
    } else {
      Shared.ircLogBotMap.getOrElse((demoUser.main.userId, demoUser.main.providerId), null)
    }
  }

  def getIrcBotByUserName(identityId: (String, String)): IrcLogBot = {
    Shared.ircLogBotMap.getOrElse(identityId, null)
  }

  def myChatFlow(sender: String, channel: String, demoUserFutOpt: Future[Option[MyEnvironment#U]]): Flow[JsValue, JsValue, _] = {
    var demoUserVar: DemoUser = null
    if (demoUserFutOpt != null) {
      demoUserVar = unpackUser(demoUserFutOpt)
    }
    Logger.debug("demoUserFutOpt.toString")

    var uniqueName: String = actorSystem.settings.config.getString("app.irc.defaultUserName")
    if (demoUserVar != null) {
      if (actorSystem.settings.config.getBoolean("app.server.users.keepUsernameAsLogin")) {
        uniqueName = demoUserVar.main.userId
      } else {
        uniqueName = sender
      }
    }

    var userActor: ActorRef = null

    // here i need to check if bot for this user already started or start new stared
    if (userActor == null) {
      userActor = actorSystem.actorOf(Props(new WebsocketUser(system = actorSystem, name = sender, demoUser = demoUserVar, channel = channel,
        ircBot = getIrcBotByUser(demoUserVar)
        //ircBot = getIrcBotByUserName(uniqueName)
      )), uniqueName)
    }

    val in =
      Flow[JsValue]
        .map(
          IrcReceivedMessage(sender, _)
        )
        .to(Sink.actorRef[IrcChatEvent](userActor, IrcParticipantLeft(sender)))

    // The counter-part which is a source that will create a target ActorRef per
    // materialization where the userActor will send its messages to.
    // This source will only buffer one element and will fail if the client doesn't read
    // messages fast enough.
    val out =
    Source.actorRef[JsValue](100, OverflowStrategy.dropNew)
      .mapMaterializedValue(
        // give the user actor a way to send messages out
        userActor ! IrcNewParticipant(sender, _)
      )
    Logger.debug(s"myChatFlow: \n${in}\n${out}")
    Flow.fromSinkAndSource(in, out)
  }

  def chat(botName: String): WebSocket = {
    Logger.debug("IrcController.chat")
    WebSocket.acceptOrResult[JsValue, JsValue] {
      case request if sameOriginCheck(request) =>

        var demoUserFutOpt: Future[Option[MyEnvironment#U]] = null
        demoUserFutOpt = SecureSocial.currentUser(request, env, executionContext)
        Future.successful(myChatFlow(botName, actorSystem.settings.config.getString("app.irc.defaultChannel"), demoUserFutOpt)).map { flow =>
          Right(flow)
        }
          .recover {
            case e: Exception =>
              val msg = "Cannot create websocket"
              Logger.error(msg, e)
              val result = InternalServerError(msg)
              Left(result)
          }

      case rejected =>
        Logger.error(s"Request ${rejected} failed same origin check")
        Future.successful {
          Left(Forbidden("forbidden"))
        }
    }
  }

  /**
    * Checks that the WebSocket comes from the same origin.  This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    *
    * See https://tools.ietf.org/html/rfc6455#section-1.3 and
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    */
  private def sameOriginCheck(rh: RequestHeader): Boolean = {
    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        Logger.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        Logger.error(s"originCheck: rejecting request because Origin header value ${
          badOrigin
        } is not in the same origin")
        false

      case None =>
        Logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    */
  private def originMatches(origin: String): Boolean = {
    try {
      val url = new URL(origin)
      url.getHost == "localhost" &&
        (url.getPort match {
          case 9000 | 19001 => true;
          case _ => false
        })
    } catch {
      case e: Exception => false
    }
  }

}
