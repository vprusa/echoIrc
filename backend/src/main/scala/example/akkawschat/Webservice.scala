package example.akkawschat

import java.util.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.stage._

import scala.concurrent.duration._

import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.stream.scaladsl.Flow

import upickle.default._
import shared.Protocol
import shared.Protocol._

import scala.concurrent.{ Future }

//import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{ Success, Failure }

import org.pircbotx.{ Configuration, PircBotX }

class Webservice(implicit fm: Materializer, system: ActorSystem) extends Directives {
  //  val theChat = Chat.create(system)
  //val theChat = Chat.create(system)

  //val ircChat = IrcChat
  /*
  import system.dispatcher

  system.scheduler.schedule(15.second, 15.second) {
    theChat.injectMessage(ChatMessage(sender = "clock", s"Bling! The time is ${new Date().toString}."))
  }*/

  val server: String = "localhost"
  val channel: String = "#TheName"

  //val botListener: IrcBotListener2 = new IrcBotListener2(theChat, server, channel)

  val listener: IrcBotListener2 = new IrcBotListener2(system, server, channel)

  val config = new Configuration.Builder()
    .addAutoJoinChannel(channel)
    .setServer(server, 6667)
    .addListener(listener)
    .setName(System.getProperty("bot.name", "ircLogBot"))
    .setRealName(System.getProperty("bot.name", "ircLogBot") + " (http://git.io/v3twr)")
    .setAutoReconnect(true)
    .setVersion("0.0.1")
    .setFinger("ircLogBot (source code here http://git.io/v3twr)")
    .setAutoNickChange(true)
    .setSocketTimeout(1 * 60 * 1000)
    .buildConfiguration()

  val bot: IrcLogBot = new IrcLogBot(channel, config)
  //listener.setSend(bot.send())
  listener._bot = bot
  listener._send = bot.sendIRC()

  def route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        // Scala-JS puts them in the root of the resource directory per default,
        // so that's where we pick them up
        path("frontend-launcher.js")(getFromResource("frontend-launcher.js")) ~
        path("frontend-fastopt.js")(getFromResource("frontend-fastopt.js")) ~
        path("chat") {
          parameter('name) { name ⇒
            handleWebSocketMessages(websocketChatFlow(sender = name))
          }
        }
    } ~
      getFromResourceDirectory("web")

  def websocketChatFlow(sender: String): Flow[Message, Message, Any] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) ⇒ msg // unpack incoming WS text messages...
        // This will lose (ignore) messages not received in one chunk (which is
        // unlikely because chat messages are small) but absolutely possible
        // FIXME: We need to handle TextMessage.Streamed as well.
      }
      .via(listener.chatFlow(sender)) // ... and route them through the chatFlow ...
      .map {
        case msg: Protocol.Message ⇒
          TextMessage.Strict(write(msg)) // ... pack outgoing messages into WS JSON messages ...
      }
      .via(reportErrorsFlow) // ... then log any processing errors on stdin

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .transform(() ⇒ new PushStage[T, T] {
        def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)

        override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
          println(s"WS stream failed with $cause")
          super.onUpstreamFailure(cause, ctx)
        }
      })

  import scala.concurrent._
  import ExecutionContext.Implicits.global

  println("starting calculation ...")
  val f = Future {
    bot.startBot()
    bot.send()
    listener._bot = bot
    listener._send = bot.sendIRC()
    0
  }
  println("before onComplete")
  f.onComplete {
    case Success(value) => println(s"Got the callback, meaning = $value")
    case Failure(e)     => e.printStackTrace
  }

  //def getConfig(server: String, channel: String): Configuration =
}
