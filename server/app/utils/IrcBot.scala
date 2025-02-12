package utils

import akka.actor.ActorRef
import models.LogWrapper
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.types.GenericChannelEvent
import org.pircbotx.{Configuration, PircBotX}
import play.api.Logger
import shared.SharedMessages.{JsMessageRequestTargetsParticipants, JsMessageResponseTargetsParticipants, TargetParticipant}

class IrcLogBot(config: Configuration) extends PircBotX(config) {

  //val TRUSTSTORE_NAME = "cacerts.jks"

  var _defaultListener: IrcListener = null

  // Getter IrcListener
  def defaultListener = _defaultListener

  // Setter IrcListener
  def defaultListener_=(value: IrcListener): Unit = _defaultListener = value

}

class IrcListener(server: String, channels: java.util.List[String], identity: (String, String), var listenersUserActor: ActorRef) extends ListenerAdapter {

  var logHandlers = Map.empty[String, LogWrapper]

  def setUserActor(newListenersUserActor: ActorRef): Unit = {
    listenersUserActor = newListenersUserActor
  }

  def getCurrentLog(target: String): LogWrapper = {
    if (logHandlers.contains(target)) {
      logHandlers.get(target).get
    } else {
      val logs: LogWrapper = new LogWrapper(identity, target)
      logHandlers += (target -> logs)
      logs
    }
  }

  def getCurrentLog(event: GenericChannelEvent): LogWrapper = {
    getCurrentLog(event.getChannel.getName)
  }

  // this method returns JsMessageResponseTargetsParticipants containing map with targets participants described in jsmsg
  def getJsMessageResponseTargetParticipants(jsmsg: JsMessageRequestTargetsParticipants, ircBot: IrcLogBot): JsMessageResponseTargetsParticipants = {
    var map = Map.empty[String, Array[TargetParticipant]]
    val iterator = ircBot.getUserBot.getChannels.iterator()
    Logger.debug(iterator.toString)
    jsmsg.targets.foreach(target => {
      while (iterator.hasNext) {
        var participantsNames: Array[TargetParticipant] = Array.empty[TargetParticipant]
        val channel = iterator.next()
        if (channel.getName.matches(target)) {
          val channelIterator = channel.getUsers.iterator()
          while (channelIterator.hasNext) {
            val user = channelIterator.next
            participantsNames +:= TargetParticipant(name = user.getNick)
          }
        }
        map += (channel.getName -> participantsNames)
      }
    })
    JsMessageResponseTargetsParticipants(map)
  }


}
