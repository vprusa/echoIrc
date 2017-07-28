package shared

import akka.actor.ActorRef
import org.pircbotx.Configuration
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.types.{GenericChannelEvent, GenericChannelUserEvent, GenericMessageEvent}
import play.Logger
import play.api.libs.json.Json
import securesocial.core.java
import service.{DemoUser, MyEnvironment}
import shared.SharedMessages.{JsMessage, JsMessageIrcBotReady, JsMessageOther, JsMessageStarBotRequest}
import upickle.default.write
import utils.{IrcListener, IrcLogBot}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object Shared {

  var ircLogBotMap: Map[(String, String), IrcLogBot] = Map.empty[(String, String), IrcLogBot]

  private var data: String = "empty"

  def setData(d: String): Unit = data = d

  def getData: String = data

  val conf = play.api.Play.current.configuration
  val channels = conf.getStringList("app.irc.defaultChannels").get
  val server = conf.getString("app.irc.server").get

  def getIrcBotByUser(demoUser: DemoUser): Option[IrcLogBot] = {
    val ex = Shared.ircLogBotMap.getOrElse((demoUser.main.userId, demoUser.main.providerId), null)
    if (ex == null) {
      None
    } else {
      Option(ex)
    }
  }

  def createListener(uniqueName: (String, String), subscriber: ActorRef): IrcListener =
    new IrcListener(server, channels, uniqueName, subscriber) {
      override def onAction(event: ActionEvent): Unit = {
        Logger.debug(s"onAction: ${event.toString}")
        if (event.getAction == "/part" || event.getAction == "/leave") {
          Logger.debug(s"onAction if: ${event.toString}")
          // TODO leave channel
          // https://github.com/TheLQ/pircbotx/wiki/MigrationGuide2
          //event.getBot[IrcLogBot].partChannel(event.getChannel, "Goodbye")
          getCurrentLog(event).logLineAndExecuteScriptAction(JsMessage(event.getUser.getNick, event.getUser.getNick, "Leaving channel"))
          event.getChannel.send().part("Leaving")
        }
      }

      override def onGenericChannel(event: GenericChannelEvent) {
        Logger.debug(s"onGenericChannel: ${event.getChannel.getBot[IrcLogBot].getNick} ${event.toString} sub is: ${subscriber}")
        if (event.isInstanceOf[GenericMessageEvent]) {
          val chatMsgEv: GenericMessageEvent = event.asInstanceOf[GenericMessageEvent]

          val jsmsg: JsMessage = JsMessage(
            sender = chatMsgEv.getUser.getNick, target = event.getChannel.getName, msg = chatMsgEv.getMessage)
          //    Logger.debug("JsMessage")

          if (listenersUserActor != null) {
            getCurrentLog(event).logLineAndExecuteScriptAction(jsmsg)
            listenersUserActor ! Json.parse(write(jsmsg))
          } else {
            Logger.debug(s"onGenericMessage sub missing")
          }
        } else if (event.isInstanceOf[GenericChannelUserEvent]) {
          // any other kind of event, log anyway
          val eventGeneric: GenericChannelUserEvent = event.asInstanceOf[GenericChannelUserEvent]
          if (event.getClass.getSimpleName.contains("JoinEvent") || event.getClass.getSimpleName.contains("PartEvent")) {

            var targets = Array.empty[String]
            var iterator = event.getBot[IrcLogBot].getUserBot.getChannels.iterator()
            while (iterator.hasNext) {
              val n = iterator.next()
              targets +:= n.getName
            }

            val jsmsg = JsMessageStarBotRequest(event.getBot[IrcLogBot].getNick, targets)
            val resp = getJsMessageStarBotResponse(jsmsg, event.getBot[IrcLogBot])
            //sendJsMessage(resp)
            //JsMessageStarBotRequest
            listenersUserActor ! Json.parse(write(resp))
          }
          val jsmsg: JsMessageOther = JsMessageOther(
            sender = eventGeneric.getUser.getNick, target = event.getChannel.getName, msg = event.getClass.getName)

          if (listenersUserActor != null) {
            getCurrentLog(event).logLineAndExecuteScriptAction(jsmsg)
            listenersUserActor ! Json.parse(write(jsmsg))
          } else {
            Logger.debug(s"onGenericMessage sub missing")
          }
        } else {
          // any other kind of event, log anyway
          val jsmsg: JsMessageOther = JsMessageOther(
            sender = "unknown", target = event.getChannel.getName, msg = event.toString)

          if (listenersUserActor != null) {
            getCurrentLog(event).logLineAndExecuteScriptAction(jsmsg)
            // listenersUserActor ! Json.parse(write(jsmsg))
          } else {
            Logger.debug(s"onGenericMessage sub missing")
          }
        }

      }
    }

  def createAndStartIrcBot(listenerOpt: Option[IrcListener], uniqueName: (String, String)): IrcLogBot = {
    // todo requires listenerOpt not None
    val listener = listenerOpt.get

    val name = uniqueName._1

    val configParams: Configuration.Builder = Shared.getDefaultIrcConfigBuilder(name)

    // TODO change setServer() to setWebIrcHostname() & Port?
    configParams.setServer(server, 6667).setName(name).addListener(listener) //.buildConfiguration()

    val config = configParams.buildConfiguration()

    val ircBot = new IrcLogBot(config)
    ircBot._defaultListener = listener

    Shared.ircLogBotMap += (uniqueName -> ircBot)

    //Logger.debug(s"sShared.ircLogBotMap ${Shared.ircLogBotMap.toString}...")
    Logger.debug("s Start bot in future...")
    val f = Future {
      ircBot.defaultListener.listenersUserActor ! Json.parse(write(JsMessageIrcBotReady()))
      Logger.debug(s"Future -> ircBot.startBot()")
      ircBot.startBot()
      0
    }
    f.onComplete {
      case Success(value) => {
        Logger.debug(s"Got the callback, meaning = $value")
        ircBot.defaultListener.listenersUserActor ! Json.parse(write(JsMessageIrcBotReady()))
      }
      case Failure(e) => e.printStackTrace
    }
    ircBot
  }

  def getDefaultIrcConfigBuilder(name: String): Configuration.Builder = {
    val configParams: Configuration.Builder = new Configuration.Builder()
      .addAutoJoinChannels(channels)
      .setRealName(name)
      .setAutoReconnect(true)
      .setVersion("0.0.1")
      .setFinger("echoIrc (TODO source link)")
      .setAutoNickChange(true)
      .setSocketTimeout(1 * 60 * 1000)

    configParams
  }


  def unpackUser(demoUserFutOpt: Future[Option[MyEnvironment#U]]): DemoUser = {
    //todo
    var demoUserVarOpt: Option[DemoUser] = None
    Logger.debug("unpackUser")

    val maybeCurUser = for {
      f1Result <- demoUserFutOpt
    } yield (f1Result)

    maybeCurUser onComplete {
      case f => {}
    }
    maybeCurUser // wait for result

    if (maybeCurUser == None) {
      throw new NullPointerException("You must be logged in to use WebSocket (maybeCurUser == None)")
    }
    import scala.concurrent.duration.Duration
    val result = Await.result(maybeCurUser, Duration.Inf)

    if (result.isEmpty) {
      throw new NullPointerException("You must be logged in to use WebSocket (result.isEmpty)")
    }

    val someUser: Some[DemoUser] = result.asInstanceOf[Some[DemoUser]]

    someUser match {
      case Some(d: DemoUser) => {
        demoUserVarOpt = Some(d)
      }
      case other => {
        // TODO SecurityException because no DemoUser found so probably forgery
        //Logger.debug(s"SecurityException ${someUser.toString}")
        throw new Exception(s"SecurityException ${someUser.toString}")
      }
    }

    if (demoUserVarOpt.isEmpty) {
      throw new NullPointerException("NullPointerException (demoUserVarOpt.isEmpty)")
    }
    demoUserVarOpt.get
  }


}
