package settings

import akka.actor.ActorSystem
import play.Logger
import play.api._
import com.typesafe.config._
import shared._

object Global extends GlobalSettings {

  var persistanObj: String = "persistanObj"

  val system = ActorSystem("app")
  Logger.info("test1")

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    // Here I use typesafe config to get config data out of application conf
    val cfg: Config = ConfigFactory.load()
    val initialValue = cfg.getString("shared.initial")
    // set initial value for shared
    Shared.setData(initialValue)

    Logger.info("test2")
    persistanObj += "1"
    Logger.info(s"persistanObj: ${persistanObj}")

    // start default bot

   // Shared.adminIrcBot = new IrcBotBackendProcess(app)
  }

}