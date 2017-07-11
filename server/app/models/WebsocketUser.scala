package models


import org.pircbotx.Configuration
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.types.{GenericChannelEvent, GenericChannelUserEvent, GenericMessageEvent}
import play.api.Logger
import play.api.libs.json.Json
import service.DemoUser
import shared.Shared
import shared.SharedMessages.{JsMessage, JsMessageBase, _}
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Failure, Success}
//

import akka.actor.{Actor, ActorRef, ActorSystem, Terminated}
import play.api.libs.json.JsValue
import utils.{IrcListener, IrcLogBot}

import scala.concurrent.Future

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

  var userActor: ActorRef = null

  var listener: IrcListener = null

  def sendJsMessage[T <: JsMessageBase](jsmsg: T): Unit = {
    val jsval: JsValue = Json.parse(upickle.default.write[JsMessageBase](jsmsg))
    Logger.debug(jsval.toString())
    userActor ! jsval
  }

  def shouldBotPersist(): Boolean = {
    val v = system.settings.config.getBoolean("app.client.onWebJoinNewIrcJoin")
    !v
  }

  def receive = {
    case IrcNewParticipant(name, subscriber) => {

      if (ircBot != null) {
        Logger.debug(s"IrcNewParticipant( name: ${name} ,subscriber: ${subscriber} ) with running ircBot")
        // already created ircLogBot
        // update subscriber for listener
        ircBot.defaultListener.listenersUserActor = subscriber
        // welcome message - but ircbot may not be ready yet
        subscriber ! Json.parse(write[JsMessage](JsMessage(
          sender = name, target = channel, msg = "Connected to web server")))
        ircBot.defaultListener.listenersUserActor ! Json.parse(write(JsMessageIrcBotReady()))
      } else {
        Logger.debug(s"IrcNewParticipant( name: ${name} ,subscriber: ${subscriber} )")
        Logger.debug(s"this ${this} and sub ${userActor}")

        // TODO im sure that there is better way how to redirrect messages from/to websocket to/from irc
        userActor = subscriber

        // welcome message - but ircbot may not be ready yet
        subscriber ! Json.parse(write[JsMessage](JsMessage(
          sender = name, target = channel, msg = "Connected to web server")))

        var uniqueName: (String, String) = (system.settings.config.getString("app.irc.defaultUserName"), "default")
        if (demoUser != null) {
          Logger.debug(s"demoUser ${demoUser.toString}")
          uniqueName = (demoUser.main.userId, demoUser.main.providerId)
        }
        Logger.debug(s"SecureSocial User id ( name: ${name} )")
        // Logger.debug(demoUser.toString)

        listener = new IrcListener(server, channel, uniqueName, subscriber) {
          override def onAction(event: ActionEvent): Unit = {
            Logger.debug(s"onAction: ${event.toString}")
            if (event.getAction == "/part" || event.getAction == "/leave") {
              Logger.debug(s"onAction if: ${event.toString}")
              // TODO leave channel
              // https://github.com/TheLQ/pircbotx/wiki/MigrationGuide2
              //event.getBot[IrcLogBot].partChannel(event.getChannel, "Goodbye")
              getCurrentLog(event).logLineAndExecuteScriptAction(JsMessage(event.getUser.getNick, event.getUser.getNick, "Leaving channel"))
              event.getChannel.send().part("Leaving with love")
            }
          }

          override def onGenericChannel(event: GenericChannelEvent) {
            Logger.debug(s"onGenericChannel: ${event.getChannel.getBot[IrcLogBot].getNick} ${event.toString} sub is: ${subscriber}")
            if (event.isInstanceOf[GenericMessageEvent]) {
              val chatMsgEv: GenericMessageEvent = event.asInstanceOf[GenericMessageEvent]

              val jsmsg: JsMessage = JsMessage(
                sender = chatMsgEv.getUser.getNick, target = event.getChannel.getName, msg = chatMsgEv.getMessage)
              //    Logger.debug("JsMessage")

              if (listenersUserActor != null) {
                getCurrentLog(event).logLineAndExecuteScriptAction(jsmsg)
                listenersUserActor ! Json.parse(write(jsmsg))
              } else {
                Logger.debug(s"onGenericMessage sub missing")
              }
            } else if (event.isInstanceOf[GenericChannelUserEvent]) {
              // any other kind of event, log anyway
              val eventGeneric: GenericChannelUserEvent = event.asInstanceOf[GenericChannelUserEvent]
              if (event.getClass.getSimpleName.contains("JoinEvent") || event.getClass.getSimpleName.contains("PartEvent")) {

                var targets = Array.empty[String]
                var iterator = event.getBot[IrcLogBot].getUserBot.getChannels.iterator()
                while (iterator.hasNext) {
                  val n = iterator.next()
                  targets +:= n.getName
                }

                val jsmsg = JsMessageStarBotRequest(event.getBot[IrcLogBot].getNick, targets)
                val resp = getJsMessageStarBotResponse(jsmsg, event.getBot[IrcLogBot])
                //sendJsMessage(resp)
                //JsMessageStarBotRequest
                listenersUserActor ! Json.parse(write(resp))
              }
              val jsmsg: JsMessageOther = JsMessageOther(
                sender = eventGeneric.getUser.getNick, target = event.getChannel.getName, msg = event.getClass.getName)

              if (listenersUserActor != null) {
                getCurrentLog(event).logLineAndExecuteScriptAction(jsmsg)
                listenersUserActor ! Json.parse(write(jsmsg))
              } else {
                Logger.debug(s"onGenericMessage sub missing")
              }
            } else {
              // any other kind of event, log anyway
              val jsmsg: JsMessageOther = JsMessageOther(
                sender = "unknown", target = event.getChannel.getName, msg = event.toString)

              if (listenersUserActor != null) {
                getCurrentLog(event).logLineAndExecuteScriptAction(jsmsg)
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

      val incommingMsg = read[JsMessageBase](msg.message.toString())

      if (userActor != null) {
        Logger.debug(s"sub is: ${userActor}")
        //userActor ! msg.message
        incommingMsg match {
          case jsmsg: JsMessage => {
            // handle the JsMessage
            Logger.debug(s"JsMessage")

            // log to file
            ircBot.defaultListener.getCurrentLog(jsmsg.target).logLineAndExecuteScriptAction(jsmsg)
            // send to irc
            ircBot.send().message(jsmsg.target, s"${jsmsg.msg}")
            sendJsMessage(jsmsg)
          }
          case jsmsg: JsMessageJoinChannelRequest => {
            // handle the JsMessageJoinChannel
            Logger.debug(s"JsMessageJoinChannel")
            ircBot.sendIRC().joinChannel(jsmsg.target)

            sendJsMessage(JsMessageJoinChannelResponse(jsmsg.sender, jsmsg.target, Array.empty[TargetParticipant]))
          }
          case jsmsg: JsMessageRotateLogs => {
            // handle the JsMessageRotateLogs
            Logger.debug(s"JsMessageRotateLogs")
            ircBot.defaultListener.getCurrentLog(jsmsg.target).rotateNow() // = new Logs(jsmsg.sender)
            sendJsMessage(jsmsg)
          }
          case jsmsg: JsMessageLeaveChannel => {
            Logger.debug("JsMessageLeaveChannel")
            // handle the JsMessageLeaveChannel
            // TODO how?
            /*  ircBot.send().ctcpCommand(jsmsg.target, "/part") //action(jsmsg.target, "/part")
              ircBot.send().ctcpResponse(jsmsg.target, "/part") //action(jsmsg.target, "/part")
              ircBot.send().action(jsmsg.target, "/part") //action(jsmsg.target, "/part")
              ircBot.send().ctcpCommand(jsmsg.target, "/leave") //action(jsmsg.target, "/part")
              ircBot.send().ctcpResponse(jsmsg.target, "/leave") //action(jsmsg.target, "/part")
              ircBot.send().action(jsmsg.target, "/leave") //action(jsmsg.target, "/part")

              ircBot.send().ctcpCommand(jsmsg.sender, "/part") //action(target, "/part")
              ircBot.send().ctcpResponse(jsmsg.sender, "/part") //action(target, "/part")
              ircBot.send().action(jsmsg.sender, "/part") //action(target, "/part")
              ircBot.send().ctcpCommand(jsmsg.sender, "/leave") //action(target, "/part")
              ircBot.send().ctcpResponse(jsmsg.sender, "/leave") //action(target, "/part")
              ircBot.send().action(jsmsg.sender, "/leave") //action(target, "/part")
            */
            //ircBot.send().part("/leave") //action(target, "/part")
            //userActor ! jsmsg
            sendJsMessage(JsMessageLeaveChannelResponse(jsmsg.sender, jsmsg.target))
          }
          case jsmsg: JsMessageStarBotRequest => {
            Logger.debug("JsMessageStarBotRequest")

            var resp = ircBot.defaultListener.getJsMessageStarBotResponse(jsmsg, ircBot)
            sendJsMessage(resp)

          }
          case jsmsg: JsMessageStarBotResponse => {
            // nothing
            Logger.debug("JsMessageStarBotResponse")
          }

        }

      } else {
        Logger.debug(s"sub missing")
      }

    }

    case IrcParticipantLeft(person) ⇒ {
      Logger.debug(s"ParticipantLeft(person) ")

      // keep the bot running?
      if (!shouldBotPersist()) {
        ircBot.stopBotReconnect()
        ircBot.close()
      }

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
