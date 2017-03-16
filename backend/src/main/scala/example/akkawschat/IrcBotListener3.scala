package example.akkawschat

import java.net._
import java.util.regex.Pattern

import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.{ MessageEvent, PrivateMessageEvent }
import org.slf4j.{ Logger, LoggerFactory }

//import upickle.default._
import shared.Protocol._

class IrcBotListener3(var chat: Chat, var server: String, var channel: String) extends ListenerAdapter {

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  val ECHO_PATTERN = Pattern.compile("(?i)echo[ ]+(.+)")

  def IrcBotListener2(chat: Chat, server: String, channel: String) {
    this.chat = chat
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

    chat.injectMessage(ChatMessage(sender = "ircB", s"${event.getUser.getNick} ${event.getMessage}"))
  }

  override def onPrivateMessage(msg: PrivateMessageEvent) {
    msg.respond(s"Hey ${msg.getUser.getNick}, you'll find the logs on http://$host/logs")
  }

  def host = InetAddress.getLocalHost.getHostAddress

}
