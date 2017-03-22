package example.akkawschat

import java.util.Date

import akka.NotUsed
import akka.actor.{ PoisonPill, Props, ActorSystem }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.stage._
import org.slf4j.{ LoggerFactory, Logger }

import scala.concurrent.duration._

import akka.http.scaladsl.server.Directives
import akka.stream.{ OverflowStrategy, Materializer }
import akka.stream.scaladsl.{ Source, Sink, Flow }

import upickle.default._
import shared.Protocol
import shared.Protocol._

import scala.concurrent.{ Future }

//import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{ Success, Failure }

import org.pircbotx.{ Configuration, PircBotX }

class Webservice(implicit fm: Materializer, system: ActorSystem) extends Directives {

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  val server: String = system.settings.config.getString("app.irc.server") //"localhost"
  val channel: String = system.settings.config.getString("app.irc.defaultChannel") //"#TheName"

  val listener: WebListener = new WebListener(system, server, channel)

  /*
    val listener: WebAndIrcListener = new WebAndIrcListener(system, server, channel)

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

    val bot: IrcLogBot = new IrcLogBot(config)
    listener._bot = bot

    import scala.concurrent._
    import ExecutionContext.Implicits.global


    println("s Start bot in future...")
    val f = Future {
      bot.startBot()

      //listener._bot = bot
      0
    }
    println("")
    f.onComplete {
      case Success(value) => println(s"Got the callback, meaning = $value")
      case Failure(e)     => e.printStackTrace
    }
  */

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
            handleWebSocketMessages(newUser(sender = name))
          }
        }
    } ~
      getFromResourceDirectory("web")

  val chatRoom = system.actorOf(Props(new ChatRoom), "chat")

  def newUser(sender: String): Flow[Message, Message, NotUsed] =
    {
      // new connection - new user actor
      val userActor = system.actorOf(Props(new User(name = sender)))

      val incomingMessages: Sink[Message, NotUsed] =
        Flow[Message].map {
          // transform websocket message to domain message
          case TextMessage.Strict(text) => User.IncomingMessage(text)
        }.to(Sink.actorRef[User.IncomingMessage](userActor, PoisonPill))

      val outgoingMessages: Source[Message, NotUsed] =
        Source.actorRef[User.OutgoingMessage](10, OverflowStrategy.fail)
          .mapMaterializedValue { outActor =>
            // give the user actor a way to send messages out
            userActor ! User.Connected(outActor)
            NotUsed
          }.map(
            // transform domain message to web socket message
            (outMsg: User.OutgoingMessage) => TextMessage(outMsg.text)
          )

      // then combine both to a flow
      Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
    }

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
  /*

  println("s Start bot in future...")
  val f = Future {
    bot.startBot()

    //listener._bot = bot
    0
  }
  println("")
  f.onComplete {
    case Success(value) => println(s"Got the callback, meaning = $value")
    case Failure(e)     => e.printStackTrace
  }

*/
  //def getConfig(server: String, channel: String): Configuration =
}
