package example.akkawschat

import scala.concurrent.{ Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Success, Failure }
import org.pircbotx.output.{ OutputIRC }

import org.pircbotx.{ Configuration, PircBotX }

class IrcLogBot(defaultChannel: String, config: Configuration) extends PircBotX(config) {

  val TRUSTSTORE_NAME = "cacerts.jks"

  /*def IrcLogBot(config: Configuration): Unit = {
    val listener: IrcBotListener2 = config.getListenerManager()
    val listener.send: OutputIRC = sendIRC()
  }*/

  def IrcLogBot(defaultChannel: String, config: Configuration) {
    _defaultChannel = defaultChannel
  }

  var _defaultChannel: String = null

  // Getter
  def defaultChannel = _defaultChannel

  // Setter
  def defaultChannel_=(value: String): Unit = _defaultChannel = value

}
/*
object IrcBot {

  println("starting calculation ...")
  val f = Future {
    val server: String = "localhost"
    val channel: String = "#TheName"

    val botListener: IrcBotListener2 = new IrcBotListener2(server, channel)
    val bot: PircBotX = new IrcLogBot(getConfig(server, channel), botListener)
    bot.startBot()
    42
  }
  println("before onComplete")
  f.onComplete {
    case Success(value) => println(s"Got the callback, meaning = $value")
    case Failure(e)     => e.printStackTrace
  }

  def getConfig(server: String, channel: String): Configuration =
    new Configuration.Builder()
      .addAutoJoinChannel(channel)
      .setServer(server, 6667)
      .addListener(new IrcBotListener2(server, channel))
      .setName(System.getProperty("bot.name", "ircLogBot"))
      .setRealName(System.getProperty("bot.name", "ircLogBot") + " (http://git.io/v3twr)")
      .setAutoReconnect(true)
      .setVersion("0.0.1")
      .setFinger("ircLogBot (source code here http://git.io/v3twr)")
      .setAutoNickChange(true)
      .setSocketTimeout(1 * 60 * 1000)
      .buildConfiguration()
}
*/ 