package example.akkawschat

import scala.concurrent.{ Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Success, Failure }

import org.pircbotx.{ Configuration, PircBotX }

class IrcLogBot(config: Configuration, listener: IrcBotListener2) extends PircBotX(config) {

  val TRUSTSTORE_NAME = "cacerts.jks"

  def IrcLogBot(bot: IrcBotListener2) {
    setNick(System.getProperty("bot.name", "ircLogBot"))
  }

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