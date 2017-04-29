package models


import java.util.concurrent.TimeUnit
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

object WebsocketUser {

  sealed trait IrcChatEvent

  case class IrcNewParticipant(name: String, subscriber: ActorRef) extends IrcChatEvent

  case class IrcParticipantLeft(name: String) extends IrcChatEvent

  case class IrcReceivedMessage(sender: String, message: JsValue) extends IrcChatEvent {
  }

}

class WebsocketUser(system: ActorSystem, name: String, channel: String, var demoUser: DemoUser, var ircBot: IrcLogBot = null) extends Actor {

  import WebsocketUser._

  val server = system.settings.config.getString("app.irc.server")

  var sub: ActorRef = null

  var listener: IrcListener = null

  def receive = {
    case IrcNewParticipant(name, subscriber) => {

      if (ircBot != null) {
        // already created ircLogBot
        // update subscriber for listener
        ircBot.defaultListener.listenersUserActor = subscriber
        // welcome message - but ircbot may not be ready yet
        subscriber ! Json.parse(write[JsMessage](JsMessage(
          sender = name, target = channel, msg = "Connected to web server")))
        ircBot.defaultListener.listenersUserActor ! Json.parse(write(JsMessageIrcBotReady()))
      } else {
        Logger.debug(s"IrcNewParticipant( name: ${name} ,subscriber: ${subscriber} )")
        Logger.debug(s"this ${this} and sub ${sub}")

        // TODO im sure that there is better way how to redirrect messages from/to websocket to/from irc
        sub = subscriber

        // welcome message - but ircbot may not be ready yet
        subscriber ! Json.parse(write[JsMessage](JsMessage(
          sender = name, target = channel, msg = "Connected to web server")))

        var uniqueName: String = system.settings.config.getString("app.irc.defaultUserName")
        if (demoUser != null) {
          Logger.debug(s"demoUser ${demoUser.toString}")
          uniqueName = demoUser.main.userId
        }

        listener = new IrcListener(server, channel, uniqueName, subscriber) {
          override def onAction(event: ActionEvent): Unit = {
            Logger.debug(s"onAction: ${event.toString}")
            if (event.getAction == "/part" || event.getAction == "/leave") {
              Logger.debug(s"onAction if: ${event.toString}")
              // TODO leave channel
              // https://github.com/TheLQ/pircbotx/wiki/MigrationGuide2
              //event.getBot[IrcLogBot].partChannel(event.getChannel, "Goodbye")
              logs.logLine("Leaving channel")
              event.getChannel.send().part("Leaving with love")
            }
          }

          override def onGenericChannel(event: GenericChannelEvent) {
            Logger.debug(s"onGenericChannel: ${event.getChannel.getBot[IrcLogBot].getNick} ${event.toString} sub is: ${subscriber}")
            if (event.isInstanceOf[GenericMessageEvent]) {
              val chatMsgEv: GenericMessageEvent = event.asInstanceOf[GenericMessageEvent]

              val jsmsg: JsMessage = JsMessage(
                sender = chatMsgEv.getUser.getNick, target = event.getChannel.getName, msg = chatMsgEv.getMessage)

              if (listenersUserActor != null) {
                logs.logLine(jsmsg.toString)
                listenersUserActor ! Json.parse(write(jsmsg))
              } else {
                Logger.debug(s"onGenericMessage sub missing")
              }
            } else if (event.isInstanceOf[GenericChannelUserEvent]) {
              // any other kind of event, log anyway
              val eventGeneric: GenericChannelUserEvent = event.asInstanceOf[GenericChannelUserEvent]
              val jsmsg: JsMessageOther = JsMessageOther(
                sender = eventGeneric.getUser.getNick, target = event.getChannel.getName, msg = event.getClass.getName)

              if (listenersUserActor != null) {
                logs.logLine(jsmsg.toString)
                listenersUserActor ! Json.parse(write(jsmsg))
              } else {
                Logger.debug(s"onGenericMessage sub missing")
              }
            } else {
              // any other kind of event, log anyway
              val jsmsg: JsMessageOther = JsMessageOther(
                sender = "unknown", target = event.getChannel.getName, msg = event.toString)

              if (listenersUserActor != null) {
                logs.logLine(jsmsg.toString)
                // listenersUserActor ! Json.parse(write(jsmsg))
              } else {
                Logger.debug(s"onGenericMessage sub missing")
              }
            }

          }
        }

        val configParams: Configuration.Builder = new Configuration.Builder()
          .addAutoJoinChannel(channel)
          .setRealName(name)
          .setAutoReconnect(true)
          .setVersion("0.0.1")
          .setFinger("echoIrc (TODO source link)")
          .setAutoNickChange(true)
          .setSocketTimeout(1 * 60 * 1000)

        // TODO change setServer() to setWebIrcHostname() & Port?
        configParams.setServer(server, 6667).setName(name).addListener(listener) //.buildConfiguration()

        val config = configParams.buildConfiguration()

        ircBot = new IrcLogBot(config)
        ircBot._defaultListener = listener

        Shared.ircLogBotMap += (uniqueName -> ircBot)

        Logger.debug(s"sShared.ircLogBotMap ${Shared.ircLogBotMap.toString}...")
        Logger.debug("s Start bot in future...")
        val f = Future {
          ircBot.defaultListener.listenersUserActor ! Json.parse(write(JsMessageIrcBotReady()))
          Logger.debug(s"Future -> ircBot.startBot()")
          ircBot.startBot()
          0
        }
        f.onComplete {
          case Success(value) => {
            Logger.debug(s"Got the callback, meaning = $value")
            ircBot.defaultListener.listenersUserActor ! Json.parse(write(JsMessageIrcBotReady()))
          }
          case Failure(e) => e.printStackTrace
        }
      }
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
      // val incommingMsgClass = incommingMsg.getClass

      Logger.debug(s"incommingMsg ${incommingMsg}")

      incommingMsg match {
        case JsMessageStarBot(botName, autoJoinChannels) => {
          Logger.debug(s"JsMessageStarBot")
        }
        case JsMessage(sender, target, msg) => {
          // handle the JsMessage
          Logger.debug(s"JsMessage")
          // log to file
          ircBot.defaultListener.logs.logLine(JsMessage(sender, target, msg).toString)
          // send to irc
          ircBot.send().message(target, s"${msg}")
        }
        case JsMessageJoinChannel(sender, target) => {
          // handle the JsMessageJoinChannel
          Logger.debug(s"JsMessageJoinChannel")
          ircBot.sendIRC().joinChannel(target)
        }
        case JsMessageLeaveChannel(sender, target) => {
          // handle the JsMessageLeaveChannel
          // TODO how?
          ircBot.send().ctcpCommand(target, "/part") //action(target, "/part")
          ircBot.send().ctcpResponse(target, "/part") //action(target, "/part")
          ircBot.send().action(target, "/part") //action(target, "/part")
          ircBot.send().ctcpCommand(target, "/leave") //action(target, "/part")
          ircBot.send().ctcpResponse(target, "/leave") //action(target, "/part")
          ircBot.send().action(target, "/leave") //action(target, "/part")

          ircBot.send().ctcpCommand(sender, "/part") //action(target, "/part")
          ircBot.send().ctcpResponse(sender, "/part") //action(target, "/part")
          ircBot.send().action(sender, "/part") //action(target, "/part")
          ircBot.send().ctcpCommand(sender, "/leave") //action(target, "/part")
          ircBot.send().ctcpResponse(sender, "/leave") //action(target, "/part")
          ircBot.send().action(sender, "/leave") //action(target, "/part")

          //ircBot.send().part("/leave")//action(target, "/part")
        }
      }
    }

    case IrcParticipantLeft(person) ⇒ {
      Logger.debug(s"ParticipantLeft(person) ")

      // keep the bot running?
      //ircBot.stopBotReconnect()
      //ircBot.close()

      import akka.actor.PoisonPill
      this.self ! PoisonPill.getInstance
    }
    case Terminated(sub) ⇒ {
      Logger.debug(s"Terminated(sub) ")
      ircBot.stopBotReconnect()
      ircBot.close()
    }
    case msg: JsValue => {
      Logger.debug(s"JsValue ${msg}")
      //sender() ! msg
    }
    case msg => {
      Logger.debug(s"msg: ${msg}")
    }

  }

}
