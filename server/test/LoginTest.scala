import java.net.URI
import play.api.test.FakeRequest
import scala.language.postfixOps
import io.backchat.hookup.{Connected, DefaultHookupClient, Disconnected, HookupClientConfig, JsonMessage, Success, TextMessage}
import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mock.Mockito
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Logger
import play.api.test._
import shared.SharedMessages.{JsMessageBase, JsMessageRequestTargetsParticipants}
import scala.collection.mutable.ListBuffer

//class LoginTest extends Specification {
object LoginTest extends PlaySpecification /* or whatever OneApp */
  //with CSRFTest with Mockito with Results
{

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
      bodyText matches (".*websocketUrl.*")
    }

    "make WebSocket accessible" in new WithServer(app = GuiceApplicationBuilder().configure("logger.application" -> "DEBUG").build(), port = 9000) {
      Logger.debug("Test websocket")
      LoginUtil.login
      //LoginUtil.cookie

      val loggedRequest = FakeRequest(Helpers.GET, "/react").withCookies(LoginUtil.cookie)

      //val user = SecureSocial.currentUser(loggedRequest, app.injector.instanceOf[MyEnvironment] , app.injector.instanceOf[ExecutionContext])

      val testName = "testName"

      val uri = URI.create(s"ws://localhost:9000/chat")

      Logger.debug("uri")
      Logger.debug(uri.toString)

      val tokenHeaderAndTokenNameAndValue = LoginUtil.getTokenNameValue()


      val hookupConfig = HookupClientConfig(uri = uri, initialHeaders = FakeRequest(Helpers.GET, "/").withCookies(LoginUtil.cookie).headers.toSimpleMap)

      Logger.debug("hookupConfig")
      Logger.debug(hookupConfig.toString)

      Logger.debug("LoginUtil.cookie")
      Logger.debug(LoginUtil.cookie.toString)

      Logger.debug("hookupConfig.initialHeaders")
      Logger.debug(hookupConfig.initialHeaders.toString)

      val hookupClient = new DefaultHookupClient(hookupConfig) {
        val messages = ListBuffer[String]()

        def receive = {
          case Connected =>
            Logger.debug("Connected")

          case Disconnected(_) =>
            Logger.debug("Disconnected")

          case JsonMessage(json) =>
            Logger.debug("Json message = " + json)

          case TextMessage(text) =>
            messages += text
            Logger.debug("Text message = " + text)
        }

        connect() onSuccess {
          case Success => {
            Logger.debug("connect Success")
            // send(JsMessageStarBotRequest("testName",Array.empty[String]))
            // val jsval: JsValue = Json.parse(upickle.default.write[JsMessageBase](JsMessageStarBotRequest("testName", Array.empty[String])))
            val jsval = upickle.default.write[JsMessageBase](JsMessageRequestTargetsParticipants(testName, Array.empty[String]))
            Logger.debug(jsval.toString())
            send(jsval)
            messages += ""
          }
        }
      }
      Logger.debug("hookupClient.messages")
      Logger.debug(hookupClient.toString)
      Logger.debug(hookupClient.messages.toString)
      hookupClient.messages.contains("") must beTrue.eventually
      Logger.debug(hookupClient.messages.toString)
    }

  }

}