import org.specs2.execute.AsResult
import org.specs2.execute.Result
import play.api.test.{FakeRequest, RouteInvokers, WithApplication, Writeables}
import play.api.mvc.Cookie
import play.api.test.Helpers
import play.api.test.Helpers.cookies
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.{Application, Logger}
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import play.api.Application
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}

import javax.inject.{Inject, Singleton}

import org.specs2.mutable.Specification
import org.specs2.mutable._
import play.api.test.WithApplication
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc._
import securesocial.core._
import play.Play
import play.api.test.Helpers.defaultAwaitTimeout
import controllers.{RestController, routes}
import play.api.Logger

import play.api.Application
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import play.api.mvc._
import play.api.test._
import scala.concurrent.Future


import scala.language.postfixOps

trait CSRFTest {
  def addToken[T](fakeRequest: FakeRequest[T])(implicit app: Application) = {
    val csrfConfig = app.injector.instanceOf[CSRFConfigProvider].get
    val csrfFilter = app.injector.instanceOf[CSRFFilter]
    val token = csrfFilter.tokenProvider.generateToken

    fakeRequest.copyFakeRequest(tags = fakeRequest.tags ++ Map(
      Token.NameRequestTag -> csrfConfig.tokenName,
      Token.RequestTag -> token
    )).withHeaders((csrfConfig.headerName, token))
  }
}


object LoginUtil extends RouteInvokers with Writeables with CSRFTest {


  var _cookie: Cookie = _

  def cookie = _cookie

  def login() {
    val loginPageRequest = FakeRequest(Helpers.GET, "/custom/login")
    val loginRequest = FakeRequest(Helpers.POST, "/auth/authenticate/userpass")
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
    _cookie = idCookie.get
  }
}

abstract class WithAppLogin extends WithApplication {
  override def around[T: AsResult](t: => T): Result = super.around {
    LoginUtil.login()
    t
  }
}