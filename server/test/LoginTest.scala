
import LoginUtil.route
import org.specs2.mutable.Specification
import org.specs2.mutable._
import play.api.test.WithApplication
import play.api.test._
import play.api.test.Helpers.{cookies, defaultAwaitTimeout, _}
import play.api.mvc._
import securesocial.core._
import play.Play
import controllers.{RestController, routes}
import dao.{TokenDAO, UserDAO}
import play.api.Logger
import play.api.Application
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import play.api.mvc._
import play.api.test._
import securesocial.core.services.SaveMode
import service.InDBUserService

import scala.concurrent.Future
import scala.language.postfixOps

//class LoginTest extends Specification {
object LoginTest extends PlaySpecification /* or whatever OneApp */ with CSRFTest {

  "restBase" should {

    "should be accessible" in new WithAppLogin {

      val userviceNoApp = Application.instanceCache[InDBUserService]
      /*
            val userDao = Application.instanceCache[UserDAO]
            val tokenDao = Application.instanceCache[TokenDAO]

            app2dao(app)
            new InDBUserService(tokenDao, userDao)
            */
      val uservice = userviceNoApp(app)

      uservice.save(
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
        SaveMode.SignUp
      )

      //setup
      Logger.info("setup")
      //val fakeRequest = addToken(FakeRequest(/* params */))

      val loginPageRequest = addToken(FakeRequest(Helpers.GET, "/custom/login"))
      val loginRequest = addToken(FakeRequest(Helpers.POST, "/auth/authenticate/userpass"))
        .withFormUrlEncodedBody(("username", "test"), ("password", "userpass"))


      Logger.debug("login page")
      val loginPage = route(loginPageRequest).get
      Logger.debug(loginPage.toString)

      val loginPagesCookies = cookies(loginPage)
      Logger.debug("credentials2.toString")
      Logger.debug(loginPagesCookies.toString)

      val loginRequestWithCookies = loginRequest.withCookies(loginPagesCookies.get("PLAY_SESSION").get)
      Logger.debug("loginRequestWithCookies")
      Logger.debug(loginRequestWithCookies.toString())
      Logger.debug("login")
      Logger.debug(loginRequest.toString())
      val credentials = cookies(route(loginRequestWithCookies).get)
      Logger.debug(credentials.toString)
      val idCookie = credentials.get("id")
      Logger.debug(idCookie.toString)


      //val request = FakeRequest().withCookies(LoginUtil.cookie)
      //Logger.debug(request.toString)

      //exercise
      //val result = RestController.test()(request)//(request)
      //Logger.debug(result.toString)

      //verify
      //status(result) must equalTo(OK)
      // contentAsString(result) must contain("Welcome to the dashboard!")
    }

  }

}