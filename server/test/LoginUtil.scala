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
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import play.api.mvc._
import play.api.test._
import service.MyEnvironment

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
  val loginRequest = FakeRequest(Helpers.POST, "/auth/authenticate/userpass")
    .withFormUrlEncodedBody(("username", "owner"), ("password", "password"))
  var _cookie: Cookie = _

  def cookie = _cookie

  def login() {
    val credentials = cookies(route(loginRequest).get)
    val idCookie = credentials.get("id")
    _cookie = idCookie.get
  }

}

abstract class WithAppLogin extends WithApplication with Mockito {
  override def around[T: AsResult](t: => T): Result = super.around {
    LoginUtil.login()

    implicit val actorSystem = ActorSystem("testActorSystem", ConfigFactory.load())
    //implicit val actorSystem  = mock[ActorSystem]
    implicit val env = mock[MyEnvironment]
    implicit val webJarAssets = mock[WebJarAssets]
    implicit val messagesApi = mock[MessagesApi]


    t
  }
}