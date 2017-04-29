package shared

import akka.actor.ActorRef
import org.pircbotx.Configuration
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.types.{GenericChannelEvent, GenericChannelUserEvent, GenericMessageEvent}
import play.api.Logger
import play.api.libs.json.Json
import shared.SharedMessages.{JsMessage, JsMessageIrcBotReady, JsMessageOther}
import upickle.default.write
import utils.{IrcListener, IrcLogBot}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import service.DemoUser

/**
  * Created by vprusa on 4/16/17.
  */
class IrcBotBackendProcess(server: String, channel: String, demoUser: DemoUser, name: String) {
/*
  def startBotFromConfig(): Unit ={

  }

  def setUserActor(newListenersUserActor: ActorRef): Unit = {
    listener.setUserActor(newListenersUserActor)
  }

  // welcome message - but ircbot may not be ready yet
  val listener: IrcListener = new IrcListener(server, channel, demoUser.main.userId, null) {
    override def onAction(event: ActionEvent): Unit = {
      Logger.debug(s"onAction: ${event.toString}")
      if (event.getAction == "/part" || event.getAction == "/leave") {
        // TODO leave channel
        // https://github.com/TheLQ/pircbotx/wiki/MigrationGuide2
        //event.getBot[IrcLogBot].partChannel(event.getChannel, "Goodbye")
        logs.logLine("Leaving channel")
        event.getChannel.send().part("Leaving with love")
      }
    }

    override def onGenericChannel(event: GenericChannelEvent) {
      Logger.debug(s"onAction: ${event.toString}")
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

  var configParams: Configuration.Builder = new Configuration.Builder()
    .addAutoJoinChannel(channel)
    .setRealName(name)
    .setAutoReconnect(true)
    .setVersion("0.0.1")
    .setFinger("echoIrc (TODO source link)")
    .setAutoNickChange(true)
    .setSocketTimeout(1 * 60 * 1000)


  // TODO change setServer() to setWebIrcHostname() & Port?
  configParams.setServer(server, 6667).setName(name).addListener(listener) //.buildConfiguration()
  Logger.debug(s"NewParicipant ${name}")

  val config = configParams.buildConfiguration()

  val ircBot: IrcLogBot = new IrcLogBot(config)
  ircBot._defaultListener = listener

  println("s Start bot in future...")
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
*/
}
