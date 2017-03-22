package example.akkawschat

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

class MyActorRef(val actorRef: ActorRef, val listener: IrcListener) {
}

class IrcListener(server: String, channel: String) extends ListenerAdapter {

  //Usage: for getting configuration and sending messages from Websocket chat in child classes
  var _bot: IrcLogBot = null

  // Getter IrcLogBot
  def bot = _bot

  // Setter IrcLogBot
  def bot_=(value: IrcLogBot): Unit = _bot = value

  // so i could send messages from IRC to Webclient
  var _actorRef: ActorRef = null

  // Getter ActorRef
  def actorRef = _actorRef

  // Setter ActorRef
  def actorRef_=(value: ActorRef): Unit = _actorRef = value

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  val ECHO_PATTERN = Pattern.compile("(?i)echo[ ]+(.+)")

  //TODO:  override def onGenericMessage(event: GenericMessageEvent)
  override def onGenericMessage(event: GenericMessageEvent) {

    logger.info(s"onGenericMessage: ${event.getUser.getNick} ${event.getMessage}")

    _actorRef ! ChatMessage(sender = s"${event.getUser.getNick}", s"${event.getMessage}")

    // injectMessage(ChatMessage(sender = s"${event.getUser.getNick}", s"${event.getUser.getNick} ${event.getMessage}"))
  }

}

trait Chat {
  def chatFlow(sender: String): Flow[String, Protocol.Message, Any]

  def injectMessage(message: Protocol.ChatMessage): Unit
}

object Chat {

  val server: String = "localhost"
  val channel: String = "#TheName"

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  class MyActor extends Actor {
    var subscribers = Set.empty[(String, ActorRef)]
    //  var subscribers = Set.empty[(String, ActorRef)]

    def receive: Receive = {

      case NewParticipant(name, subscriber) ⇒

        //subscriber.
        /*
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

                // var listener: IrcListener = new IrcListener(server, channel)
                // var listener: IrcListener = subscriber.listener

                var ircBot: IrcLogBot = new IrcLogBot(configParams.setName(name).addListener(listener).buildConfiguration())

                logger.info(s"NewParicipant ${name}")

                configParams.setName(name)

                val config = configParams.buildConfiguration()

                ircBot = new IrcLogBot(config)

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
        */
        logger.info(s" msg ${context}")

        context.watch(subscriber)
        subscribers += (name -> subscriber)
        dispatch(Protocol.Joined(name, members))
      case msg: ReceivedMessage ⇒ {
        //val entry@(name, ref) = subscribers.find(_._1 == msg.sender).get

        //listener._bot.send().message(channel, s"${msg.sender} ${msg.message}")

        dispatch(msg.toChatMessage)
      }
      case msg: Protocol.ChatMessage ⇒ dispatch(msg)
      case ParticipantLeft(person) ⇒
        val entry @ (name, ref) = subscribers.find(_._1 == person).get
        // report downstream of completion, otherwise, there's a risk of leaking the
        // downstream when the TCP connection is only half-closed
        ref ! Status.Success(Unit)
        subscribers -= entry
        dispatch(Protocol.Left(person, members))
      case Terminated(sub) ⇒
        // clean up dead subscribers, but should have been removed when `ParticipantLeft`
        subscribers = subscribers.filterNot(_._2 == sub)
    }

    def sendAdminMessage(msg: String): Unit = dispatch(Protocol.ChatMessage("admin", msg))

    def dispatch(msg: Protocol.Message): Unit = subscribers.foreach(_._2 ! msg)

    def members = subscribers.map(_._1).toSeq
  }

  def create(system: ActorSystem): Chat = {
    // The implementation uses a single actor per chat to collect and distribute
    // chat messages. It would be nicer if this could be built by stream operations
    // directly.
    val chatActor: ActorRef =
      //      system.actorOf(Props(new MyActor(listener = new IrcListener(server, channel))))
      system.actorOf(Props(new MyActor))

    // Wraps the chatActor in a sink. When the stream to this sink will be completed
    // it sends the `ParticipantLeft` message to the chatActor.
    // FIXME: here some rate-limiting should be applied to prevent single users flooding the chat
    def chatInSink(sender: String) = Sink.actorRef[ChatEvent](chatActor, ParticipantLeft(sender))

    new Chat {
      def chatFlow(sender: String): Flow[String, Protocol.ChatMessage, Any] = {
        val in =
          Flow[String]
            .map(ReceivedMessage(sender, _))
            .to(chatInSink(sender))

        // The counter-part which is a source that will create a target ActorRef per
        // materialization where the chatActor will send its messages to.
        // This source will only buffer one element and will fail if the client doesn't read
        // messages fast enough.
        val out =
          Source.actorRef[Protocol.ChatMessage](1, OverflowStrategy.fail)
            .mapMaterializedValue(chatActor ! NewParticipant(sender, _))

        /*
       val out =
         Source.actorRef[Protocol.ChatMessage](1, OverflowStrategy.fail)
           .mapMaterializedValue(chatActor.tell(NewParticipant(
             sender,
             //new MyActorRef(_._: ActorRef, new IrcListener(server, channel))
             new MyActorRef(chatActor,null)
           //_
           ), _))
*/
        logger.info(s"\n\n\n\n\n ll ${in} \n${out}\n")
        Flow.fromSinkAndSource(in, out)
      }

      def injectMessage(message: Protocol.ChatMessage): Unit = chatActor ! message // non-streams interface
    }
  }

  private sealed trait ChatEvent

  private case class NewParticipant(name: String, subscriber: ActorRef) extends ChatEvent

  private case class ParticipantLeft(name: String) extends ChatEvent

  private case class ReceivedMessage(sender: String, message: String) extends ChatEvent {
    def toChatMessage: Protocol.ChatMessage = Protocol.ChatMessage(sender, message)
  }

}
