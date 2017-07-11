
import javax.inject.Inject

import LoginUtil.route
import org.specs2.mutable.Specification
import org.specs2.mutable._
import play.api.test.WithApplication
import play.api.test._
import play.api.test.Helpers.{cookies, defaultAwaitTimeout, _}
import play.api.mvc._
import securesocial.core._
import play.Play
import controllers.{RestController, WebJarAssets, routes}
import dao.{TokenDAO, UserDAO}
import play.api.Logger
import play.api.Application
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import play.api.mvc._
import play.api.test._
import securesocial.core.services.SaveMode
import service.{InDBUserService, MyEnvironment}

import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import org.specs2.mutable.Specification
import org.specs2.mutable._
import play.api.test.WithApplication
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc._
import securesocial.core._
import play.Play
import controllers.routes._
import play.api.test.Helpers.defaultAwaitTimeout
import org.specs2.mutable._
import org.joda.time.DateTime
import play.api.Logger
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mock.Mockito
import play.api.i18n.MessagesApi

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.stream.Process.Await


//class LoginTest extends Specification {
object LoginTest extends PlaySpecification /* or whatever OneApp */ with CSRFTest with Mockito with Results {

  "Login " should {

    "make Rest controller accessible" in new WithAppLogin with Mockito with MustThrownExpectations {

      val request = FakeRequest(Helpers.GET, "/rest/test").withCookies(LoginUtil.cookie)
      //Logger.debug("request.toString()")
      //Logger.debug(request.toString())
      val restTestPage = route(app, request).get
      //Logger.debug("restTestPage.toString()")
      val bodyText: String = contentAsString(restTestPage)
      //Logger.debug("bodyText.toString()")
      //Logger.debug(bodyText)
      status(restTestPage) mustEqual OK
      bodyText must be equalTo "test SecuredAction"
    }

    "make ReactJs controller accessible" in new WithAppLogin with Mockito with MustThrownExpectations {
      val request = FakeRequest(Helpers.GET, "/react").withCookies(LoginUtil.cookie)
      //Logger.debug("request.toString()")
      //Logger.debug(request.toString())
      val reactPage = route(app, request).get
      //Logger.debug("restTestPage.toString()")
      val bodyText: String = contentAsString(reactPage)
      //Logger.debug("bodyText.toString()")
      //Logger.debug(bodyText)
      status(reactPage) mustEqual OK
      bodyText matches(".*websocketUrl.*")
    }

  }

}