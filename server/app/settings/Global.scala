package settings

import akka.actor.ActorSystem
import com.typesafe.config._
import org.mindrot.jbcrypt.BCrypt
import service.DemoUser
//import org.specs2.matcher.MustThrownExpectations
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Before
import play.api._
import play.api.data.Form
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import securesocial.controllers.ViewTemplates
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services.{AvatarService, SaveMode, UserService}
import securesocial.core.{AuthenticationMethod, BasicProfile}
import shared._
import utils.Planner

import scala.concurrent.ExecutionContext.Implicits.global
//import org.specs2.mutable.Specification
import dao.UserDAO
import play.api.Application
//import play.api.test.WithApplicationLoader
import play.Logger
import securesocial.core._
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}
//import utils.

object Global extends GlobalSettings {

  var persistentObj: String = "persistentObj"

  val system = ActorSystem("app")
  //Logger.info("Global")

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    // Here I use typesafe config to get config data out of application conf
    val cfg: Config = ConfigFactory.load()
    val initialValue = cfg.getString("shared.initial")
    // set initial value for shared
    Shared.setData(initialValue)

    // Logger.info(s"persistentObj: ${persistentObj}")

    import service._
    val envCache = Application.instanceCache[MyEnvironment]

    // Logger.debug("envCache.toString()")
    // Logger.debug(envCache.toString())
    val env = envCache(app)
    // Logger.debug(env.toString())
    val upp = env.providers.getOrElse("userpass", new UsernamePasswordProvider[DemoUser](env.userService, env.avatarService, env.viewTemplates, env.passwordHashers))
    val conf = play.api.Play.current.configuration

    val ownerUsername = conf.getString("app.server.users.owner.username").getOrElse(null)
    val ownerPassword = conf.getString("app.server.users.owner.password").getOrElse(null)
    if (!ownerUsername.isEmpty && !ownerPassword.isEmpty) {
      val futDemoU = env.userService.save(
        BasicProfile(
          upp.id,
          ownerUsername,
          Some(""),
          Some(""),
          Some(""),
          Some(ownerUsername + "@localhost"),
          None,
          AuthenticationMethod.UserPassword,
          None,
          None,
          Some(PasswordInfo("bcrypt", BCrypt.hashpw(ownerPassword, BCrypt.gensalt(12))))
        ), SaveMode.SignUp
      )

      val futDemoURes = Await.result(futDemoU, 5 seconds)
      Logger.debug("Added owner user")
      //Logger.debug(futDemoURes.toString())
    }

    val app2dao = Application.instanceCache[UserDAO]
    val dao: UserDAO = app2dao(app)

    //import scala.concurrent.duration.DurationInt
    //import scala.concurrent.ExecutionContext.Implicits.global
    //import scala.concurrent.{Await, Future}

    val storedUsers = Await.result(dao.all(), 1 seconds)
    Logger.debug("Stored Users list")
    Logger.debug(storedUsers.toString())

    val planner = new Planner(app, cfg)
    planner.planFutureLogRotation()
  }

}