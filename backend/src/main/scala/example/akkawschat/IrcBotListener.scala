package example.akkawschat

import java.net._
import java.util.regex.Pattern

import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.{ MessageEvent, PrivateMessageEvent }
import org.slf4j.{ Logger, LoggerFactory }

class IrcBotListener(var server: String, var channel: String) extends ListenerAdapter {

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getName)

  val ECHO_PATTERN = Pattern.compile("(?i)echo[ ]+(.+)")

  def IrcBotListener(server: String, channel: String) {
    this.server = server
    this.channel = channel
  }

  //  override def onGenericMessage(event: GenericMessageEvent[IrcLogBot]) {
  override def onMessage(event: MessageEvent) {
    if (event.getUser().getNick().toLowerCase().contains("bot")) {
      return
    }
    // do nothing, just log the message
    logger.info(s"${event.getUser.getNick} ${event.getMessage}")
  }

  override def onPrivateMessage(msg: PrivateMessageEvent) {
    msg.respond(s"Hey ${msg.getUser.getNick}, you'll find the logs on http://$host/logs")
  }

  def host = InetAddress.getLocalHost.getHostAddress

}
