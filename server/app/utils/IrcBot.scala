package utils

import java.util.regex.Pattern

import org.pircbotx.hooks.ListenerAdapter

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import org.pircbotx.output.OutputIRC
import org.pircbotx.{Configuration, PircBotX}
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Status, Terminated}

class IrcLogBot(config: Configuration) extends PircBotX(config) {

  val TRUSTSTORE_NAME = "cacerts.jks"

}


class IrcListener(server: String, channel: String, val listenersUerActor: ActorRef) extends ListenerAdapter {

  //Usage: for getting configuration and sending messages from Websocket chat in child classes
  var _bot: IrcLogBot = null

  // Getter IrcLogBot
  def bot = _bot

  // Setter IrcLogBot
  def bot_=(value: IrcLogBot): Unit = _bot = value

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  val ECHO_PATTERN = Pattern.compile("(?i)echo[ ]+(.+)")

}
