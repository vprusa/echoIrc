package controllers

import javax.inject.Singleton

import org.pircbotx.Configuration
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.types.{GenericChannelEvent, GenericMessageEvent}
import play.api.Logger
import play.api.libs.json.Json
import shared.SharedMessages.{JsMessageBase, _}
import upickle.default._

import scala.util.{Failure, Success}
//

import java.net.URL
import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import utils.{IrcListener, IrcLogBot, Protocol}

import scala.concurrent.{ExecutionContext, Future}

// http://stackoverflow.com/questions/37371698/could-not-find-implicit-value-for-parameter-messages-play-api-i18n-messages-in

/**
  * A very simple chat client using websockets.
  */
@Singleton
class IrcWebController @Inject()(implicit actorSystem: ActorSystem, webJarAssets: WebJarAssets,
                                 mat: Materializer,
                                 executionContext: ExecutionContext, override val messagesApi: MessagesApi)
  extends BaseController with I18nSupport {

  def ircChat: Action[AnyContent] = Action { implicit request =>
    val url = routes.IrcWebController.chat().webSocketURL()
    Ok(views.html.ircChat(url))
  }

  object User {

    case class Connected(outgoing: ActorRef)

    case class IncomingMessage(text: String)

    case class OutgoingMessage(text: String)

  }

  class User(system: ActorSystem, name: String, channel: String) extends Actor {

    import User._

    val server = system.settings.config.getString("app.irc.server")

    var sub: ActorRef = null

    var listener: IrcListener = null

    def receive = {
      case IrcNewParticipant(name, subscriber) => {
        Logger.debug(s"IrcNewParticipant( name: ${name} ,subscriber: ${subscriber} )")
        Logger.debug(s"this ${this} and sub ${sub} ")

        // TODO im sure that there is better way how to redirrect messages from/to websocket to/from irc
        sub = subscriber

        // welcome message
        subscriber ! Json.parse(write[JsMessage](JsMessage(
          sender = name, target = channel, msg = "Welcome!!!")))

        listener = new IrcListener(server, channel, subscriber) {
          override def onAction(event: ActionEvent): Unit ={
            Logger.debug(s"onAction: ${event.toString}")
            Logger.debug(s"onAction getAction: ${event.getAction}")
            Logger.debug(s"onAction getMessage: ${event.getMessage}")
            if(event.getAction == "/part" || event.getAction == "/leave"){
              Logger.debug(s"onAction if: ${event.toString}")
              // TODO leave channel
              // https://github.com/TheLQ/pircbotx/wiki/MigrationGuide2
              //event.getBot[IrcLogBot].partChannel(event.getChannel, "Goodbye")
              event.getChannel.send().part("Leaving with love")
            }
          }
          override def onGenericChannel(event: GenericChannelEvent) {
            Logger.debug(s"onGenericChannel: ${event.getChannel.getBot[IrcLogBot].getNick} ${event.toString} sub is: ${subscriber}")
            val chatMsgEv: GenericMessageEvent = event.asInstanceOf[GenericMessageEvent]
            Logger.debug(s"onGenericChannel1 casted to GenericMessageEvent ${chatMsgEv}")
            Logger.debug(s"onGenericChannel2 casted to GenericMessageEvent ${chatMsgEv.getMessage}")
            Logger.debug(s"onGenericChannel3 channel ${event.getChannel.getName} ${event.getChannel.toString}")
            if (listenersUserActor != null) {
              listenersUserActor ! Json.parse(write(JsMessage(
                sender = chatMsgEv.getUser.getNick, target = event.getChannel.getName, msg = chatMsgEv.getMessage)))
            } else {
              Logger.debug(s"onGenericMessage sub missing")
            }
          }
        }

        var configParams: Configuration.Builder = new Configuration.Builder()
          .addAutoJoinChannel(channel)
          .setServer(server, 6667)
          .setRealName(name)
          .setAutoReconnect(true)
          .setVersion("0.0.1")
          .setFinger("ircLogBot (TODO source link)")
          .setAutoNickChange(true)
          .setSocketTimeout(1 * 60 * 1000)

        var ircBot: IrcLogBot = new IrcLogBot(configParams.setName(name).addListener(listener).buildConfiguration())

        Logger.debug(s"NewParicipant ${name}")

        configParams.setName(name)

        val config = configParams.buildConfiguration()

        ircBot = new IrcLogBot(config)

        listener._bot = ircBot

        import scala.concurrent._
        import ExecutionContext.Implicits.global

        println("s Start bot in future...")
        val f = Future {
          listener.listenersUserActor ! Json.parse(write(JsMessageIrcBotReady()))
          Logger.debug(s"Future -> ircBot.startBot()")
          ircBot.startBot()
          0
        }
        f.onComplete {
          case Success(value) => {
            Logger.debug(s"Got the callback, meaning = $value")
            listener.listenersUserActor  ! Json.parse(write(JsMessageIrcBotReady()))
          }
          case Failure(e) => e.printStackTrace
        }

        //Protocol.Joined(name, members)
      }
      case msg: IrcReceivedMessage ⇒ {
        Logger.debug(s"msg: IrcReceivedMessage ${msg} jsvalues: ${msg.message}")
        Logger.debug(s"this ${this} ")
        Logger.debug(s"sub  ${sub} ")

        if (sub != null) {
          Logger.debug(s"sub is: ${sub}")
          sub ! msg.message
        } else {
          Logger.debug(s"sub missing")
        }
        Logger.debug(s"Sending message to irc")

        val incommingMsg = read[JsMessageBase](msg.message.toString())
        val incommingMsgClass = incommingMsg.getClass

        Logger.debug(s"incommingMsg ${incommingMsg}")

        incommingMsg match {
          case JsMessage(sender, target, msg) => {
            // handle the JsMessage
            listener._bot.send().message(target, s"${msg}")
          }
          case JsMessageJoinChannel(sender, target) => {
            // handle the JsMessageJoinChannel
            Logger.debug(s"JsMessageJoinChannel")
            listener._bot.sendIRC().joinChannel(target)
          }
          case JsMessageLeaveChannel(sender, target) => {
            // handle the JsMessageLeaveChannel
            // TODO how?
          /*  listener._bot.sendIRC().ctcpCommand(target,"/part")//action(target, "/part")
            listener._bot.sendIRC().ctcpResponse(target,"/part")//action(target, "/part")
            listener._bot.sendIRC().action(target,"/part")//action(target, "/part")
            listener._bot.sendIRC().ctcpCommand(target,"/leave")//action(target, "/part")
            listener._bot.sendIRC().ctcpResponse(target,"/leave")//action(target, "/part")
            listener._bot.sendIRC().action(target,"/leave")//action(target, "/part")

            listener._bot.sendIRC().ctcpCommand(sender,"/part")//action(target, "/part")
            listener._bot.sendIRC().ctcpResponse(sender,"/part")//action(target, "/part")
            listener._bot.sendIRC().action(sender,"/part")//action(target, "/part")
            listener._bot.sendIRC().ctcpCommand(sender,"/leave")//action(target, "/part")
            listener._bot.sendIRC().ctcpResponse(sender,"/leave")//action(target, "/part")
            listener._bot.sendIRC().action(sender,"/leave")//action(target, "/part")*/
          }
        }
      }
      case msg => {
        Logger.debug(s"msg: ${msg}")
      }
      case msg: Protocol.ChatMessage ⇒ {
        Logger.debug(s"msg: Protocol.ChatMessage ")
        //dispatch(msg)
      }
      case IrcParticipantLeft(person) ⇒ {
        Logger.debug(s"ParticipantLeft(person) ")
        listener._bot.stopBotReconnect()
        listener._bot.close()
      }
      case Terminated(sub) ⇒ {
        Logger.debug(s"Terminated(sub) ")
        listener._bot.stopBotReconnect()
        listener._bot.close()
      }

      case Connected(outgoing) => {
        Logger.debug(s"Connected(outgoing) ")

        // context.become(connected(outgoing))
      }
      case msg: Protocol.ChatMessage => {
        Logger.debug(s"Protocol.ChatMessage ")
        //sender() ! msg

      }
      case msg: JsValue => {
        Logger.debug(s"JsValue ${msg}")
        //sender() ! msg
      }

    }

  }

  private sealed trait IrcChatEvent

  private case class IrcNewParticipant(name: String, subscriber: ActorRef) extends IrcChatEvent

  private case class IrcParticipantLeft(name: String) extends IrcChatEvent

  private case class IrcReceivedMessage(sender: String, message: JsValue) extends IrcChatEvent {
    def toChatMessage: Protocol.ChatMessage = Protocol.ChatMessage(sender, message.toString())
  }


  class MyActor extends Actor {
    def receive: Receive = {
      case msg => {
        Logger.debug(s"MyActor msg: ${msg}")
      }

    }
  }

  def myChatFlow(sender: String, channel: String): Flow[JsValue, JsValue, _] = {
    val userActor: ActorRef = actorSystem.actorOf(Props(new User(system = actorSystem, name = sender, channel = channel)))
    Logger.debug("IrcController.myChatFlow")

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

  def chat: WebSocket = {
    Logger.debug("IrcController.chat")
    WebSocket.acceptOrResult[JsValue, JsValue] {
      case rh if sameOriginCheck(rh) =>
        //Future.successful(websocketChatFlow("userBot","#TheName")).map { flow =>
        Future.successful(myChatFlow("userBot", "#TheName")).map { flow =>
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
