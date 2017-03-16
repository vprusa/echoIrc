package example.akkawschat

import java.net._
import java.util.regex.Pattern

import akka.NotUsed
import akka.actor._
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Sink, Source, Flow }
import example.akkawschat.Chat.{ ParticipantLeft, NewParticipant, ReceivedMessage }
import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.{ MessageEvent, PrivateMessageEvent }
import org.pircbotx.output.{ OutputIRC }
import org.slf4j.{ Logger, LoggerFactory }

//import upickle.default._

import shared.Protocol
import shared.Protocol._

abstract class ChatAndIrcBotListener extends ListenerAdapter {
  def chatFlow(sender: String): Flow[String, Protocol.Message, Any]

  def injectMessage(message: Protocol.ChatMessage): Unit

  // def setSend(send: OutputIRC): Unit

  //val send: OutputIRC

}

class IrcBotListener2(system: ActorSystem, var server: String, var channel: String) extends ChatAndIrcBotListener {

  var _bot: IrcLogBot = null
  var _send: OutputIRC = null

  // Getter
  def bot = _bot

  // Setter
  def bot_=(value: IrcLogBot): Unit = _bot = value

  // Getter
  def send = _send

  // Setter
  def send_=(value: OutputIRC): Unit = _send = value

  // The implementation uses a single actor per chat to collect and distribute
  // chat messages. It would be nicer if this could be built by stream operations
  // directly.
  val chatActor =
    system.actorOf(Props(new Actor {
      var subscribers = Set.empty[(String, ActorRef)]

      def receive: Receive = {
        case NewParticipant(name, subscriber) ⇒
          context.watch(subscriber)
          subscribers += (name -> subscriber)
          dispatch(Protocol.Joined(name, members))
        case msg: ReceivedMessage      ⇒ dispatch(msg.toChatMessage)
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
    }))

  // Wraps the chatActor in a sink. When the stream to this sink will be completed
  // it sends the `ParticipantLeft` message to the chatActor.
  // FIXME: here some rate-limiting should be applied to prevent single users flooding the chat
  def chatInSink(sender: String): Sink[ChatEvent, akka.NotUsed] = Sink.actorRef[ChatEvent](chatActor, ParticipantLeft(sender))

  //  private trait ChatEvent escapes its defining scope as part of

  //def chatFlow(sender: String): Flow[String, Protocol.ChatMessage, Any] = {
  def chatFlow(sender: String): Flow[String, Protocol.Message, Any] = {
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

    Flow.fromSinkAndSource(in, out)
  }

  def injectMessage(message: Protocol.ChatMessage): Unit = {
    chatActor ! message
  }

  // non-streams interface

  sealed trait ChatEvent

  case class NewParticipant(name: String, subscriber: ActorRef) extends ChatEvent

  case class ParticipantLeft(name: String) extends ChatEvent

  case class ReceivedMessage(sender: String, message: String) extends ChatEvent {
    def toChatMessage: Protocol.ChatMessage = {

      // injectMessage(ChatMessage(sender, s"${message}"))
      //_bot.send().message(_bot._defaultChannel, s"3.  ${sender} ${message}")
      _bot.send().message("#TheName", s"3.  ${sender} ${message}")

      logger.info(s"${_bot} ${_bot.send()} ${_bot.sendIRC()}")

      //_bot.getUserBot().send().message(s"3.  ${sender} ${message}")

      logger.info(s"${sender} ${message}")
      Protocol.ChatMessage(sender, message)

    }

  }

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  val ECHO_PATTERN = Pattern.compile("(?i)echo[ ]+(.+)")

  def IrcBotListener2(system: ActorSystem, server: String, channel: String) {
    //this.system = system
    this.server = server
    this.channel = channel

  }

  /*  def this(chat: Chat, server: String, channel: String) {
    this(server: String, channel: String)

  }
*/
  //  override def onGenericMessage(event: GenericMessageEvent[IrcLogBot]) {
  override def onMessage(event: MessageEvent) {

    if (event.getUser().getNick().toLowerCase().contains("bot")) {
      return
    }
    // do nothing, just log the message
    logger.info(s"test msg")
    logger.info(s"${event.getUser.getNick} ${event.getMessage}")
    //    _send.message()
    //    _bot.sendIRC().message(_bot._defaultChannel, s"2. ${event.getUser.getNick} ${event.getMessage}")

    injectMessage(ChatMessage(sender = "ircB", s"${event.getUser.getNick} ${event.getMessage}"))
  }

  override def onPrivateMessage(msg: PrivateMessageEvent) {
    msg.respond(s"Hey ${msg.getUser.getNick}, you'll find the logs on http://$host/logs")
  }

  def host = InetAddress.getLocalHost.getHostAddress

}
