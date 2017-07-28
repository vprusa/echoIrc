package controllers

import java.net.URL
import javax.inject.{Inject, Singleton}

import akka.actor.{ActorNotFound, ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import models.WebsocketUser
import models.WebsocketUser._
import play.Logger
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
import scala.concurrent.duration.Duration
import shared.Shared

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


  def myChatFlow(demoUser: DemoUser): Flow[JsValue, JsValue, _] = {
    Logger.debug("myChatFlow.toString")

    // actorSystem.settings.config.getString("app.irc.defaultUserName")

    var userActor: Option[ActorRef] = None

    import scala.concurrent.duration._
    implicit val timeout = 10.seconds

    try {
      val existingActorFut = actorSystem.actorSelection(s"akka://application/user/${demoUser.main.userId}").resolveOne(timeout)
      val result = Await.result(existingActorFut, Duration.Inf)
      //Logger.debug(s"result")
      //Logger.debug(result.toString)

      if (result != null) {
        userActor = Some(result)
      }
    } catch {
      case e: ActorNotFound => Logger.debug(s"ActorNotFound: WS Actor for ${demoUser.main.userId} not found")
      case e: Exception => Logger.debug(s"Exception when looking for existing WS Actor with name: ${demoUser.main.userId}")
    }
    //try {
    // here i need to check if bot for this user already started or start new stared
    if (userActor.isEmpty) {
      userActor = Some(actorSystem.actorOf(Props(new WebsocketUser(system = actorSystem, demoUser = demoUser
        //,ircBot = getIrcBotByUser(demoUser)
        //ircBot = getIrcBotByUserName(uniqueName)
      )), demoUser.main.userId))

    }

    val in =
      Flow[JsValue]
        .map(
          IrcReceivedMessage(demoUser.main.userId, _)
        )
        .to(Sink.actorRef[IrcChatEvent](userActor.get, IrcParticipantLeft(demoUser.main.userId)))

    // The counter-part which is a source that will create a target ActorRef per
    // materialization where the userActor will send its messages to.
    // This source will only buffer one element and will fail if the client doesn't read
    // messages fast enough.
    val out =
    Source.actorRef[JsValue](100, OverflowStrategy.dropNew)
      .mapMaterializedValue(
        // give the user actor a way to send messages out
        // todo requires not None
        userActor.get ! IrcNewParticipant(demoUser.main.userId, _)
      )
    Logger.debug(s"myChatFlow: \n${in}\n${out}")
    Flow.fromSinkAndSource(in, out)
  }

  def chat: WebSocket = {
    Logger.debug("IrcController.chat")
    WebSocket.acceptOrResult[JsValue, JsValue] {
      case request if sameOriginCheck(request) =>
        Logger.debug("IrcWebController.chat.request")
        Logger.debug(request.toString())
        Logger.debug(request.headers.toString())
        Logger.debug(request.headers.toSimpleMap.toString())
        Logger.debug("request.cookies.toString()")
        Logger.debug(request.cookies.toString())
        Logger.debug("request.session.toString()")
        Logger.debug(request.session.toString())

        Logger.debug("env.toString()")
        Logger.debug(env.toString())

        Logger.debug("env.toString()")
        Logger.debug(env.toString())


        Logger.debug("executionContext.toString()")
        Logger.debug(executionContext.toString())

        Future.successful(myChatFlow(Shared.unpackUser(SecureSocial.currentUser(request, env, executionContext)))).map { flow =>
          Right(flow)
        }.recover {
          case e: Exception =>
            val msg = "Can not create WebSocket"
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
