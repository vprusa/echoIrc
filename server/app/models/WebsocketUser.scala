package models


import java.util

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

class WebsocketUser(system: ActorSystem, demoUser: DemoUser) extends Actor {

  import WebsocketUser._

  val server = system.settings.config.getString("app.irc.server")

  var userActorOpt: Option[ActorRef] = None

  var listenerOpt: Option[IrcListener] = None

  var ircBotOpt: Option[IrcLogBot] = Shared.getIrcBotByUser(demoUser)

  def sendJsMessage[T <: JsMessageBase](jsmsg: T): Unit = {
    val jsval: JsValue = Json.parse(upickle.default.write[JsMessageBase](jsmsg))
    Logger.debug(jsval.toString())
    userActorOpt.map(userActor => {
      userActor ! jsval
    })
  }

  def shouldBotPersist(): Boolean = {
    //val v = system.settings.config.getBoolean("app.client.onWebJoinNewIrcJoin")
    val v = system.settings.config.getBoolean(s"app.server.users.${demoUser.main.userId}.alwaysRunning")
    !v
  }

  def receive = {
    case IrcNewParticipant(name, subscriber) => {
      val conf = play.api.Play.current.configuration
      val channels = conf.getStringList("app.irc.defaultChannels")

      if (!ircBotOpt.isEmpty) {
        Logger.debug(s"IrcNewParticipant( name: ${name} ,subscriber: ${subscriber} ) with running ircBot")
        // already created ircLogBot
        // update subscriber for listener
        ircBotOpt.map(ircBot => {
          // welcome message - but ircbot may not be ready yet
          //        subscriber ! Json.parse(write[JsMessage](JsMessage(
          //          sender = name, target = channel, msg = "Connected to web server")))

          ircBot.defaultListener.listenersUserActor = subscriber
          ircBot.defaultListener.listenersUserActor ! Json.parse(write(JsMessageIrcBotReady()))
        })
      } else {
        Logger.debug(s"IrcNewParticipant( name: ${name} ,subscriber: ${subscriber} )")
        Logger.debug(s"this ${this} and sub ${userActorOpt}")

        // TODO im sure that there is better way how to redirrect messages from/to websocket to/from irc
        userActorOpt = Some(subscriber)

        // welcome message - but ircbot may not be ready yet
        //        subscriber ! Json.parse(write[JsMessage](JsMessage(
        //          sender = name, target = channel, msg = "Connected to web server")))

        var uniqueName: (String, String) = (system.settings.config.getString("app.irc.defaultUserName"), "default")
        if (demoUser != null) {
          Logger.debug(s"demoUser ${demoUser.toString}")
          uniqueName = (demoUser.main.userId, demoUser.main.providerId)
        }
        Logger.debug(s"SecureSocial User ( name: ${name} )")
        // Logger.debug(demoUser.toString)

        listenerOpt = Some(Shared.createListener(uniqueName, subscriber))
        ircBotOpt = Some(Shared.createAndStartIrcBot(listenerOpt, uniqueName))

      }
    }
    case msg: IrcReceivedMessage ⇒ {
      Logger.debug(s"msg: IrcReceivedMessage ${msg} jsvalues: ${msg.message}")

      val incommingMsg = read[JsMessageBase](msg.message.toString())

      if (!userActorOpt.isEmpty) {
        ircBotOpt.map(ircBot => {

          Logger.debug(s"sub is: ${userActorOpt}")
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
            case jsmsg: JsMessageRequestTargetsParticipants => {
              Logger.debug("JsMessageRequestTargetsParticipants")

              var resp = ircBot.defaultListener.getJsMessageResponseTargetParticipants(jsmsg, ircBot)
              sendJsMessage(resp)
            }
            case jsmsg: JsMessageResponseTargetsParticipants => {
              // nothing
              Logger.debug("JsMessageStarBotResponse")
            }
            case jsmsg: JsMessageTestRequest => {
              // nothing
              Logger.debug("JsMessageTestRequest - replying with JsMessageTestResponse")
              sendJsMessage(JsMessageTestResponse(jsmsg.sender, jsmsg.target, jsmsg.msg))
            }
            case jsmsg: JsMessageTestResponse => {
              // nothing
              Logger.debug("JsMessageTestResponse")
            }
          }

        })

      } else {
        Logger.debug(s"sub missing")
      }

    }

    case IrcParticipantLeft(person) ⇒ {
      Logger.debug(s"ParticipantLeft(person) ")
      // keep the bot running?
      if (!shouldBotPersist()) {
        ircBotOpt.map(ircBot => {
          ircBot.stopBotReconnect()
          ircBot.close()
        })
      }
      import akka.actor.PoisonPill
      this.self ! PoisonPill.getInstance
    }
    case Terminated(sub) ⇒ {
      Logger.debug(s"Terminated(sub) ")
      ircBotOpt.map(ircBot => {
        ircBot.stopBotReconnect()
        ircBot.close()
      })
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
