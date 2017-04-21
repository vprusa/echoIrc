package utils

import java.util.regex.Pattern

import akka.actor.ActorRef
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.{Configuration, PircBotX}
import models.Logs

class IrcLogBot(config: Configuration) extends PircBotX(config) {

  //val TRUSTSTORE_NAME = "cacerts.jks"

  var _defaultListener: IrcListener = null

  // Getter IrcListener
  def defaultListener = _defaultListener

  // Setter IrcListener
  def defaultListener_=(value: IrcListener): Unit = _defaultListener = value


}

class IrcListener(server: String, channel: String, name: String, val listenersUserActor: ActorRef) extends ListenerAdapter {

  //private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  val ECHO_PATTERN = Pattern.compile("(?i)echo[ ]+(.+)")
  val logs: Logs = new Logs(name)

}
