package controllers

import javax.inject.Singleton

import org.pircbotx.Configuration
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.types.{GenericChannelEvent, GenericChannelUserEvent, GenericMessageEvent}

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.Logger
import play.api.libs.json.Json
import securesocial.core.SecureSocial
import service.{DemoUser, MyEnvironment}
import shared.Shared
import shared.SharedMessages.{JsMessageBase, _}
import upickle.default._
import utils.IrcLogBot

import scala.util.{Failure, Success}
import models.WebsocketUser._
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import utils.{IrcListener, IrcLogBot, Protocol}
import models.WebsocketUser

import scala.concurrent.{ExecutionContext, Future}

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
    //val url = routes.IrcWebController.chat().webSocketURL()
    Ok(views.html.ircChat("url"))
  }


  class MyActor extends Actor {
    def receive: Receive = {
      case msg => {
        Logger.debug(s"MyActor msg: ${msg}")
      }
    }
  }

  def unpackUser(demoUserFutOpt: Future[Option[MyEnvironment#U]]): DemoUser = {
    var demoUserVar: DemoUser = null //TODO ..hate to use null but should be safe cause of Exception below

    val maybeCurUser = for {
      f1Result <- demoUserFutOpt
    } yield (f1Result)

    Logger.debug(s"maybeCurUser.toString1 ${maybeCurUser.toString}")

    maybeCurUser onComplete {
      case f => {
        Logger.debug(s"maaybeCurUser all done ${f.toString}")
      }
    }
    Logger.debug(s"maybeCurUser1 ${maybeCurUser.toString}")
    maybeCurUser // this will make it wait for result
    Logger.debug(s"maybeCurUser2 ${maybeCurUser.toString}")
    import scala.concurrent.duration.Duration

    val result = Await.result(maybeCurUser, Duration.Inf)

    Logger.debug(s"result.toString ${result.toString}")

    Logger.debug(s"maybeCurUser.toString ${maybeCurUser.toString}")
    val someUser: Some[DemoUser] = result.asInstanceOf[Some[DemoUser]]
    Logger.debug(s"someUser.toString ${someUser.toString}")

    someUser match {
      case Some(d: DemoUser) => {
        demoUserVar = d
        Logger.debug(s"demoUser = d ${demoUserVar.toString}")
        Logger.debug(s"d = d ${d.toString}")
      }
      case other => {
        // TODO SecurityException because no DemoUser found so probably forgery
        Logger.debug(s"SecurityException ${someUser.toString}")
        throw new Exception("SecurityException")
      }
    }

    Logger.debug(s"demoUser before = null ${demoUserVar.toString}")

    if (demoUserVar == null) {
      Logger.debug(s"demoUser = null")
      throw new NullPointerException("NullPointerException")
    }
    Logger.debug(s"user.main.userId ${demoUserVar.main.userId}")

    Logger.debug(s"demoUserVar.toString2 ${demoUserVar.toString}")
    demoUserVar
  }

  def getActor(uniqueName: String, actorSystem: ActorSystem): ActorRef = {
    import scala.concurrent._
    import scala.concurrent.duration._
    import akka.util.Timeout

    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    val maybeCurActor = for {
      f1Result <- actorSystem.actorSelection(uniqueName).resolveOne()
    } yield (f1Result)

    if (maybeCurActor.isInstanceOf[Failure[akka.actor.ActorNotFound]])
      return null

    Logger.debug(s"maybeCurActor ${maybeCurActor}")
    Logger.debug(s"maybeCurActor.getClass ${maybeCurActor.getClass}")
    Logger.debug(s"maybeCurActor.getClass.getName ${maybeCurActor.getClass.getName}")
    Logger.debug(s"maybeCurActor.toString1 ${maybeCurActor.toString}")

    maybeCurActor onComplete {
      case f => {
        Logger.debug(s"maybeCurActor all done ${f.toString}")
      }
    }
    Logger.debug(s"maybeCurActor1 ${maybeCurActor.toString}")
    maybeCurActor // this will make it wait for result
    Logger.debug(s"maybeCurActor2 ${maybeCurActor.toString}")


    if (maybeCurActor.isInstanceOf[Failure[akka.actor.ActorNotFound]])
      return null

    Logger.debug(s"maybeCurActor ${maybeCurActor}")
    Logger.debug(s"maybeCurActor.getClass ${maybeCurActor.getClass}")
    Logger.debug(s"maybeCurActor.getClass.getName ${maybeCurActor.getClass.getName}")
    Logger.debug(s"maybeCurActor.toString1 ${maybeCurActor.toString}")

    import scala.concurrent.duration.Duration
    val result = Await.result(maybeCurActor, Duration.Inf)
    Logger.debug(s"result.toString3")

    Logger.debug(s"result.toString ${result.toString}")
    result
  }

  def getIrcBotByUser(demoUser: DemoUser): IrcLogBot = {
    if (demoUser == null) {
      null
    } else {
      Shared.ircLogBotMap.getOrElse(demoUser.main.userId, null)
    }
  }

  def getIrcBotByUserName(userName: String): IrcLogBot = {
    Shared.ircLogBotMap.getOrElse(userName, null)
  }

  def myChatFlow(sender: String, channel: String, demoUserFutOpt: Future[Option[MyEnvironment#U]]): Flow[JsValue, JsValue, _] = {
    var demoUserVar: DemoUser = null
    if (demoUserFutOpt != null) {
      demoUserVar = unpackUser(demoUserFutOpt)
    }
    Logger.debug("demoUserFutOpt.toString")
    Logger.debug(demoUserFutOpt.toString)
    Logger.debug(demoUserVar.toString)

    var uniqueName: String = actorSystem.settings.config.getString("app.irc.defaultUserName")
    if (demoUserVar != null) {
      uniqueName = demoUserVar.main.userId
    }

    // val userActor: ActorRef = actorSystem.actorOf(Props(new WebsocketUser(system = actorSystem, name = sender, demoUser = demoUserVar, channel = channel)), uniqueName)
    var userActor: ActorRef = null
    //userActor = getActor(uniqueName, actorSystem)
    // here i need to check if bot for this user already started or start new stared
    if (userActor == null) {
      userActor = actorSystem.actorOf(Props(new WebsocketUser(system = actorSystem, name = sender, demoUser = demoUserVar, channel = channel,
        //        ircBot = getIrcBotByUser(demoUserVar)
        ircBot = getIrcBotByUserName(uniqueName)
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

        //import play.api.Play
        //Play.configuration.
        //import Play.current
        //Play.application().configuration().getString("").
        //Future.successful(websocketChatFlow("userBot","#TheName")).map { flow =>

        // actorSystem.settings.config.getString("app.irc.server")
        var demoUserFutOpt: Future[Option[MyEnvironment#U]] = null
        if (actorSystem.settings.config.getBoolean("app.server.users.authSecureSocialEnabled")) {
          demoUserFutOpt = SecureSocial.currentUser(request, env, executionContext)
        }
        Logger.debug("demoUserFutOpt")
        Logger.debug(demoUserFutOpt.toString)
        Future.successful(myChatFlow(botName, actorSystem.settings.config.getString("app.irc.defaultChannel"), demoUserFutOpt)).map { flow =>
          Right(flow)
        }.recover {
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
        Logger.error(s"originCheck: rejecting request because Origin header value ${badOrigin} is not in the same origin")
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
