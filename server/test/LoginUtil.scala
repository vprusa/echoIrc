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

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification
import org.specs2.mutable._
import play.api.test.WithApplication
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc._
import securesocial.core._
import play.Play
import play.api.test.Helpers.defaultAwaitTimeout
import controllers.{RestController, WebJarAssets, routes}
import org.specs2.mock.Mockito
import play.api.Logger
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import play.api.mvc._
import play.api.test._
import service.MyEnvironment

import scala.concurrent.Future
import scala.language.postfixOps

trait CSRFTest {

  def getTokenNameValue()(implicit app: Application): (String, String, String) = {
    Logger.debug("CSRFTest.getTokenNameValue")
    val csrfConfig = app.injector.instanceOf[CSRFConfigProvider].get
    val csrfFilter = app.injector.instanceOf[CSRFFilter]
    val token = csrfFilter.tokenProvider.generateToken
    Logger.debug(csrfConfig.toString)
    Logger.debug(csrfConfig.headerName.toString)
    Logger.debug(csrfConfig.tokenName.toString)
    Logger.debug(csrfConfig.cookieName.toString)
    Logger.debug(csrfFilter.toString)
    Logger.debug(csrfFilter.tokenProvider.toString)
    (csrfConfig.headerName, csrfConfig.tokenName, token)
  }

  def addToken[T](fakeRequest: FakeRequest[T])(implicit app: Application) = {
    Logger.debug("CSRFTest.addToken")

    val tokenNameVal = getTokenNameValue()

    val headerName = tokenNameVal._1
    val tokenName = tokenNameVal._2
    val tokenVal = tokenNameVal._3

    Logger.debug("CSRFTest.addToken>token")
    Logger.debug(tokenName)

    fakeRequest.copyFakeRequest(tags = fakeRequest.tags ++ Map(
      Token.NameRequestTag -> tokenName,
      Token.RequestTag -> tokenVal
    )).withHeaders((headerName, tokenVal))
  }
}

object LoginUtil extends RouteInvokers with Writeables with CSRFTest {
  val username = "owner"
  val password = "password"

  val loginRequest = FakeRequest(Helpers.POST, "/auth/authenticate/userpass")
    .withFormUrlEncodedBody(("username", username), ("password", password))
  var _cookie: Cookie = _
  var _playSessionCookie: Cookie = _

  def cookie = _cookie

  def sessionCookie = _playSessionCookie

  def login(implicit app: Application) {
    Logger.debug(s"Login as user: ${username}")

    val credentials = cookies(route(addToken(loginRequest)).get)
    //val credentials = cookies(route(newLoginRequest).get)
    Logger.debug("credentials")
    Logger.debug(credentials.toString())

    val playSessionCookie = credentials.get("PLAY_SESSION")
    _playSessionCookie = playSessionCookie.get

    val idCookie = credentials.get("id")
    _cookie = idCookie.get
  }

}

abstract class WithAppLogin extends WithApplication with Mockito {

  override def around[T: AsResult](t: => T): Result = super.around {
    LoginUtil.login

    implicit val actorSystem = ActorSystem("testActorSystem", ConfigFactory.load())
    //implicit val actorSystem  = mock[ActorSystem]
    implicit val env = mock[MyEnvironment]
    implicit val webJarAssets = mock[WebJarAssets]
    implicit val messagesApi = mock[MessagesApi]


    t
  }
}

abstract class ServerWithAppLogin extends WithServer(app = GuiceApplicationBuilder().configure("logger.application" -> "DEBUG").build(), port = 9000)
  //with Mockito
//  with org.specs2.matcher.ShouldThrownExpectations
//with org.specs2.matcher.MustThrownExpectations
{

  override def around[T: AsResult](t: => T): Result = super.around {
    LoginUtil.login

    /* implicit val actorSystem = ActorSystem("testActorSystem", ConfigFactory.load())
    //implicit val actorSystem  = mock[ActorSystem]
    implicit val env = mock[MyEnvironment]
    implicit val webJarAssets = mock[WebJarAssets]
    implicit val messagesApi = mock[MessagesApi]
*/

    t
  }
  implicit val actorSystem = ActorSystem("testActorSystem", ConfigFactory.load())
  //implicit val actorSystem  = mock[ActorSystem]
  implicit val env = app.injector.instanceOf[MyEnvironment]
  implicit val webJarAssets = app.injector.instanceOf[WebJarAssets]
  implicit val messagesApi = app.injector.instanceOf[MessagesApi]

 /* override def around[T: AsResult](t: => T): Result = super.around {
    LoginUtil.login
    t
  }*/
}
