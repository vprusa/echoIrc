package example.akkawschat

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

import scala.concurrent.Channel
import scala.util.{ Success, Failure }

import org.pircbotx.{ Configuration, PircBotX }

import shared.Protocol
import shared.Protocol._

class MyActorRef(val actor: ActorRef, val listener: IrcListener) {

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

import akka.actor.{ Actor, ActorRef }

object MyActor {
  case class Connected(outgoing: ActorRef)
  case class IncomingMessage(text: String)
  case class OutgoingMessage(text: String)
}

abstract class MyActor(ircLogBot: IrcLogBot) extends Actor {

  /*
    def receive = {
      case Connected(outgoing) =>
        context.become(connected(outgoing))
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

  */
}

class WebListener(system: ActorSystem, server: String, var channel: String) {

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  //  class WebAndIrcListener(system: ActorSystem, server: String, var channel: String) extends IrcListener(system: ActorSystem, server, channel) {

  // non-streams interface

  sealed trait ChatEvent

  case class NewParticipant(name: String, subscriber: MyActorRef) extends ChatEvent

  case class ParticipantLeft(name: String) extends ChatEvent

  case class ReceivedMessage(sender: String, message: String) extends ChatEvent {
    def toChatMessage: Protocol.ChatMessage = {

      // val config = system.settings.config
      //TODO change this to currentChannel
      //val channel = config.getString("app.irc.defaultChannel")

      // Send message from Webserver to IRC target
      //_bot.send().message(channel, s"${sender} ${message}")
      // chatActor.

      //logs the message
      logger.info(s"toChatMessage : ${sender} ${message}")
      Protocol.ChatMessage(sender, message)
    }

  }

  // var server: String = system.settings.config.getString("app.irc.server") //"localhost"
  //var channel: String = system.settings.config.getString("app.irc.defaultChannel") //"#TheName"

  var configParams: Configuration.Builder = new Configuration.Builder()
    .addAutoJoinChannel(channel)
    .setServer(server, 6667)
    .setName("botName")
    .setRealName("ircBotB2")
    .setAutoReconnect(true)
    .setVersion("0.0.1")
    .setFinger("ircLogBot (source code here http://git.io/v3twr)")
    .setAutoNickChange(true)
    .setSocketTimeout(1 * 60 * 1000)

  // The implementation uses a single actor per chat to collect and distribute
  // chat messages. It would be nicer if this could be built by stream operations
  // directly.
  val chatActor: ActorRef =
    system.actorOf(Props(new Actor {

      logger.info(s"MyActor")
      var subscribers = Set.empty[(String, MyActorRef)]

      // TODO - print here so i could check if for join and incoming messages actually something happens here, if here is just websocket preparation code part
      // if preparation then to MyActor add abstract IRCChatWrapper and implement it here
      // in IRCChatWrapper instance i need access to variables (channel, name, listener?)
      // in "case NewParticipant" i need to start the IRC bot (IRCChatWrapper needs to have start bot method) 
      // change dispatch method so incoming messages will be send to chat participan adn outcomming message will be send via IRC bot

      def receive: Receive = {
        case NewParticipant(name: String, subscriber: MyActorRef) ⇒ {

          // var listener: IrcListener = new IrcListener(server, channel)
          var listener: IrcListener = subscriber.listener

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
          println("")
          f.onComplete {
            case Success(value) => println(s"Got the callback, meaning = $value")
            case Failure(e)     => e.printStackTrace
          }

          context.watch(subscriber.actor)
          subscribers += (name -> subscriber)
          dispatch(Protocol.Joined(name, members))
        }
        // startNewBot()
        case msg: ReceivedMessage ⇒ {
          logger.info(s"ReceivedMessage ${msg.sender} ${msg.message}")
          logger.info(s"subscribers ${subscribers}")

          //subscribers.foreach()
          for (sub <- subscribers) logger.info(s"subscriber name: ${sub._1}")
          /*            subscribers.foreach(_._1 {
            var namen = _._1
            logger.info(s"subscriber: ${namen}")
          })
*/
          val entry @ (name, ref) = subscribers.find(_._1 == msg.sender).get
          //msg.sender

          entry._2.listener._bot.send().message(channel, s"${msg.message}")
          //(this ActorRef)
          //dispatch(msg.toChatMessage)

          //entry._2.actor ! msg
          //subscribers.foreach(_._2.actor ! msg)

          //dispatch(message.toChatMessage)
        }
        case msg: Protocol.ChatMessage ⇒ {
          //msg

          logger.info(s"Protocol.ChatMessag ${msg.sender} ${msg.message}")

          val entry @ (name, ref) = subscribers.find(_._1 == msg.sender).get
          //msg.sender
          //entry._2.listener._bot.send().message(channel, s"${msg.sender} ${msg.message}")
          //(this ActorRef)
          // xxx entry._2.actor ! msg.message
          // subscribers.foreach(_._2.actor ! msg)

          //dispatch(messagetoChatMessage)
          dispatch(msg)
        }
        case ParticipantLeft(person) ⇒
        //val entry @ (name, ref) = subscribers.find(_._1 == person).get
        // report downstream of completion, otherwise, there's a risk of leaking the
        // downstream when the TCP connection is only half-closed
        //ref ! Status.Success(Unit)
        //subscribers -= entry
        //dispatch(Protocol.Left(person, members))
        case Terminated(sub)         ⇒
        // clean up dead subscribers, but should have been removed when `ParticipantLeft`
        //subscribers = subscribers.filterNot(_._2 == sub)
      }

      //  def sendAdminMessage(msg: String): Unit = dispatch(Protocol.ChatMessage("admin", msg))

      def dispatch(msg: Protocol.Message): Unit = {

        subscribers.foreach(_._2.actor ! msg)

        //subscribers.foreach(_._2 ! newBot.send().message(channel, s"${sender} ${msg}"))
        //subscribers.

        //subscribers.foreach(_._2 ! newBot.send().message(channel, s"${msg}"))
      }

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

    logger.info(s"\n\n\n\n\n in ${in}\n")

    // The counter-part which is a source that will create a target ActorRef per
    // materialization where the chatActor will send its messages to.
    // This source will only buffer one element and will fail if the client doesn't read
    // messages fast enough.
    val out =
      Source.actorRef[Protocol.ChatMessage](1, OverflowStrategy.fail)
        .mapMaterializedValue(chatActor.tell(NewParticipant(
          sender,
          //new MyActorRef(_._: ActorRef, new IrcListener(server, channel))
          new MyActorRef(chatActor, new IrcListener(server, channel))
        //_
        ), _))

    logger.info(s"\n\n\n\n\n ll ${in} \n${out}\n")

    Flow.fromSinkAndSource(in, out)
  }

  def injectMessage(message: Protocol.ChatMessage): Unit = {
    chatActor ! message
  }

}
