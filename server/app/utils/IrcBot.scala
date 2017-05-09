package utils

import java.util.regex.Pattern

import akka.actor.ActorRef
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.{Configuration, PircBotX}
import models.LogWrapper
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.types.{GenericChannelEvent, GenericMessageEvent}
import play.api.Logger
import shared.SharedMessages.{JsMessageStarBotRequest, JsMessageStarBotResponse, TargetParticipant}

class IrcLogBot(config: Configuration) extends PircBotX(config) {

  //val TRUSTSTORE_NAME = "cacerts.jks"

  var _defaultListener: IrcListener = null

  // Getter IrcListener
  def defaultListener = _defaultListener

  // Setter IrcListener
  def defaultListener_=(value: IrcListener): Unit = _defaultListener = value


}

class IrcListener(server: String, channel: String, identity: (String, String), var listenersUserActor: ActorRef) extends ListenerAdapter {


  // map[target,logs]
  var logHandlers = Map.empty[String, LogWrapper]
  //private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  //val ECHO_PATTERN = Pattern.compile("(?i)echo[ ]+(.+)")

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


  def getJsMessageStarBotResponse(jsmsg: JsMessageStarBotRequest, ircBot: IrcLogBot): JsMessageStarBotResponse = {
    var map = Map.empty[String, Array[TargetParticipant]]

    Logger.debug("bot not null")
    val iterator = ircBot.getUserBot.getChannels.iterator()
    Logger.debug("ircBot.getUserBot.getChannels")
    Logger.debug(ircBot.getUserBot.getChannels.toArray.toString)
    Logger.debug(ircBot.getUserBot.getChannels.toString)
    /*Logger.debug(ircBot.getUserBot.getChannels.first().toString)
    Logger.debug(ircBot.getUserBot.getChannels.first().getName)
    Logger.debug("ircBot.getUserBot.getChannels.toarr.foreach")
    ircBot.getUserBot.getChannels.toArray.foreach(i=>{
      Logger.debug(i.toString)

    })*/
    Logger.debug("iterator")
    Logger.debug(iterator.toString)
    jsmsg.targets.foreach(target => {
      Logger.debug("target")
      Logger.debug(target.toString)
      while (iterator.hasNext) {
        Logger.debug("iterator.hasNext")
        var participantsNames: Array[TargetParticipant] = Array.empty[TargetParticipant]
        val channel = iterator.next()
        Logger.debug(channel.toString)
        Logger.debug(s"channel: ${channel.getName}")
        if (channel.getName.matches(target)) {
          Logger.debug(s"hannel.getName.matches(target)")
          var channelIterator = channel.getUsers.iterator()
          Logger.debug(s"channelIterator")
          while (channelIterator.hasNext) {
            val user = channelIterator.next
            Logger.debug("user.toString")
            Logger.debug(user.toString)
            participantsNames +:= TargetParticipant(name = user.getNick)
          }
        }
        map += (channel.getName -> participantsNames)
      }
    })
    //ircBot.getUserBot.getChannels
    Logger.debug("map")
    Logger.debug(map.toString)
    JsMessageStarBotResponse(map)
  }


}
