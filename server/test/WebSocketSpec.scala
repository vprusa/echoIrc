import java.net.URI

import LoginTest.{contentAsString, route, status}
import io.backchat.hookup._
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.mutable._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Logger}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Results}
import play.api.test._
import play.filters.csrf.CSRF.Token
import securesocial.core.SecureSocial
import service.MyEnvironment
import shared.SharedMessages.{JsMessageBase, JsMessageStarBotRequest}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

//class WebSocketSpec extends Specification {
object WebSocketSpec extends PlaySpecification /* or whatever OneApp */ {
  // Override app if you need a FakeApplication with other than
  // default parameters.

  "Websocket" should {

    // todo
    // https://github.com/playframework/play-scala-websocket-example/blob/2.6.x/test/controllers/WebSocketClient.java

    // https://stackoverflow.com/questions/19306787/writing-a-unit-test-for-play-websockets/37209708#37209708
    // https://www.playframework.com/documentation/2.5.x/ScalaFunctionalTestingWithScalaTest
    //    "be able to connect" in new WithServer(app = GuiceApplicationBuilder().configure("logger.application" -> "DEBUG").build(), port = 9000) {
    "be able to connect" in new WithServer(app = GuiceApplicationBuilder().configure("logger.application" -> "DEBUG").build(), port = 9000) {
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
            val jsval = upickle.default.write[JsMessageBase](JsMessageStarBotRequest(testName, Array.empty[String]))
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