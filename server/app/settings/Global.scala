package settings

import akka.actor.ActorSystem
import play.Logger
import play.api._
import com.typesafe.config._
import dao.UserDAO
import securesocial.core.{AuthenticationMethod, BasicProfile}
import shared._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
//import org.specs2.mutable.Specification
import dao.UserDAO
import models.WebsocketUser
import play.api.Application
//import play.api.test.WithApplicationLoader
import securesocial.core._
import play.Logger


import scala.concurrent.{Await, Future}

object Global extends GlobalSettings {

  var persistanObj: String = "persistanObj"

  val system = ActorSystem("app")
  Logger.info("Global")

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    // Here I use typesafe config to get config data out of application conf
    val cfg: Config = ConfigFactory.load()
    val initialValue = cfg.getString("shared.initial")
    // set initial value for shared
    Shared.setData(initialValue)

    Logger.info(s"persistanObj: ${persistanObj}")

    // start default bot
    // Shared.adminIrcBot = new IrcBotBackendProcess(app)

    // add test user
    val app2dao = Application.instanceCache[UserDAO]
    val dao: UserDAO = app2dao(app)

    val testUsers = Set(
      BasicProfile(
        "database",
        "test",
        Some(""),
        Some(""),
        Some(""),
        Some("test@localhost"),
        None,
        AuthenticationMethod.UserPassword,
        None,
        None,
        Some(PasswordInfo("hasher", "userpass", None))
      ),
      BasicProfile(
        "testProvider",
        "user1",
        Some(""),
        Some(""),
        Some(""),
        Some("user1@demo.com"),
        None,
        AuthenticationMethod.UserPassword,
        None,
        None,
        Some(PasswordInfo("hasher", "userpass", None))
      )
    )

    Await.result(Future.sequence(testUsers.map(dao.insert)), 1 seconds)
    val storedUsers = Await.result(dao.all(), 1 seconds)
    Logger.debug("DB test")
    Logger.debug(storedUsers.toString())
  }

}