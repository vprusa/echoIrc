package example.akkawschat

import scala.concurrent.{ Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Success, Failure }
import org.pircbotx.output.{ OutputIRC }

import org.pircbotx.{ Configuration, PircBotX }

class IrcLogBot(config: Configuration) extends PircBotX(config) {

  val TRUSTSTORE_NAME = "cacerts.jks"

}