package example.akkawschat

import java.util.regex.Pattern

import akka.actor.{ Actor, ActorRef }
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.types.GenericMessageEvent
import org.slf4j.{ LoggerFactory, Logger }
import shared.Protocol.ChatMessage

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import shared.Protocol

import java.net._
import java.util.regex.Pattern

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Sink, Source, Flow }
import example.akkawschat.Chat.{ ParticipantLeft, NewParticipant, ReceivedMessage }
import example.akkawschat.IrcLogBot
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.types.{ GenericMessageEvent }
import org.pircbotx.hooks.events.{ IncomingChatRequestEvent, MessageEvent, PrivateMessageEvent }
import org.pircbotx.output.{ OutputIRC }
import org.slf4j.{ Logger, LoggerFactory }
import akka.actor.ActorRef
import scala.concurrent.Channel
import scala.util.{ Success, Failure }

import org.pircbotx.{ Configuration, PircBotX }

import shared.Protocol
import shared.Protocol._

object User {
  case class Connected(outgoing: ActorRef)
  case class IncomingMessage(text: String)
  case class OutgoingMessage(text: String)
}

class User(name: String) extends Actor {
  import User._

  val server: String = "localhost"
  val channel: String = "#TheName"

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  def receive = {
    case Connected(outgoing) => {

      var configParams: Configuration.Builder = new Configuration.Builder()
        .addAutoJoinChannel("#TheName")
        .setServer("localhost", 6667)
        .setName("botName")
        .setRealName("ircBotB2")
        .setAutoReconnect(true)
        .setVersion("0.0.1")
        .setFinger("ircLogBot (source code here http://git.io/v3twr)")
        .setAutoNickChange(true)
        .setSocketTimeout(1 * 60 * 1000)

      var listener: IrcListener = new IrcListener(server, channel)
      // var listener: IrcListener = subscriber.listener

      var ircBot: IrcLogBot = new IrcLogBot(configParams.setName(name).addListener(listener).buildConfiguration())

      logger.info(s"NewParicipant ${name}")

      listener._bot = ircBot

      import scala.concurrent._
      import ExecutionContext.Implicits.global

      println("s Start bot in future...")
      val f = Future {
        ircBot.startBot()

        //listener._bot = bot
        0
      }
      f.onComplete {
        case Success(value) => println(s"Got the callback, meaning = $value")
        case Failure(e)     => e.printStackTrace
      }

      context.become(connected(outgoing))
    }
  }

  def connected(outgoing: ActorRef): Receive = {
    chatRoom ! ChatRoom.Join

    {
      case IncomingMessage(text) =>
        chatRoom ! ChatRoom.ChatMessage(text)

      case ChatRoom.ChatMessage(text) =>
        outgoing ! OutgoingMessage(text)
    }
  }

}