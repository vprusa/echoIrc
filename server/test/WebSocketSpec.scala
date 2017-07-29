import java.net.URI

import LoginTest.{contentAsString, route, status}
import io.backchat.hookup._
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.mutable._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Results}
import play.api.test._
import play.filters.csrf.CSRF.Token
import securesocial.core.SecureSocial
import service.{DemoUser, MyEnvironment}
import shared.SharedMessages.{JsMessageBase, JsMessageRequestTargetsParticipants, JsMessageTestRequest, JsMessageTestResponse}
import upickle.default.read

import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global


object Logger {
  def debug(str: String) = {
    play.api.Logger.debug("Spec: " + str)
  }
}

object WebSocketSpec extends PlaySpecification {

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

      val tokenHeaderAndTokenNameAndValue = LoginUtil.getTokenNameValue()

      val hookupConfig = HookupClientConfig(uri = uri, initialHeaders = FakeRequest(Helpers.GET, "/").withCookies(LoginUtil.cookie).headers.toSimpleMap)

      val hookupClient = new DefaultHookupClient(hookupConfig) {
        val messages = ListBuffer[String]()
        var jsMessages = ListBuffer.empty[JsMessageBase]

        def receive = {
          case Connected => {
            Logger.debug("Connected")
          }
          case Disconnected(_) => {
            Logger.debug("Disconnected")
          }
          case JsonMessage(jsonVal) => {
            Logger.debug("Json message = " + jsonVal)
            //Logger.debug("Json message.compact(render(jsonVal)) = " + org.json4s.jackson.JsonMethods.pretty(
            //  org.json4s.jackson.JsonMethods.render(jsonVal)).toString)
            //val incommingMsg = upickle.default.read[JsMessageTestResponse](str)
            val jsMsgBase = upickle.default.read[JsMessageBase](
              org.json4s.jackson.JsonMethods.compact(
                org.json4s.jackson.JsonMethods.render(jsonVal)).toString)

            //Logger.debug("jsMsgBase " + jsMsgBase.toString)
            jsMsgBase match {
              case jsMsg: JsMessageTestResponse => {
                Logger.debug("JsMessageTestResponse " + jsMsg.toString)
                //Logger.debug("\n\n\n\n\n\nJsMessageTestResponse")
                //Logger.debug("\n\n\n\n\n\nJsMessageTestResponse")
                //Logger.debug(jsMsg.toString())
                //jsMessages :+ jsMsg
                jsMessages ++= ListBuffer(jsMsg)
              }
              case unknown => {
                Logger.debug("unknown")
                Logger.debug(unknown.toString())
              }
            }
          }
          case TextMessage(text) => {
            messages +: text
            Logger.debug("Text message = " + text)
          }
          case Reconnecting => {
            Logger.debug("Reconnecting")
          }
          case v => {
            Logger.debug("unknown message = " + v.toString)
          }
        }

        connect() onSuccess {
          case Success => {
            Logger.debug("connect Success")
            // send(JsMessageStarBotRequest("testName",Array.empty[String]))
            // val jsval: JsValue = Json.parse(upickle.default.write[JsMessageBase](JsMessageStarBotRequest("testName", Array.empty[String])))
            //            val jsval = upickle.default.write[JsMessageBase](JsMessageRequestTargetsParticipants(testName, Array.empty[String]))

            var counter = 1

            // todo wait for response? so far async in receive
            def sendTestMessageAndWaitResult(): Unit = {
              val jsval = upickle.default.write[JsMessageBase](JsMessageTestRequest(testName, testName, "testMessage " + counter))
              Logger.debug(counter + ": " + jsval.toString())
              val futRes: Future[OperationResult] = send(jsval)
              val result = Await.result(futRes, Duration.Inf)
              Logger.debug(counter + ": " + "result.toString() \n" + result.toString())
              counter += 1
            }

            sendTestMessageAndWaitResult()

            messages += ""
          }
        }

      }
      Logger.debug("hookupClient.messages")
      Logger.debug(hookupClient.toString)
      Logger.debug(hookupClient.messages.toString)

      // https://stackoverflow.com/questions/28160021/using-futures-and-thread-sleep
      // todo better way
      val f: Future[String] = Future {
        Thread.sleep(10000)
        Logger.debug("hookupClient.messages.toString")
        Logger.debug(hookupClient.messages.toString)

        Logger.debug("hookupClient.jsMessages.toString")
        Logger.debug(hookupClient.jsMessages.toString)

        Logger.debug("hookupClient.disconnect()")
        hookupClient.disconnect()
        "future value"
      }

      Await.ready(f, 60 seconds)

      hookupClient.jsMessages.toString.contains("testMessage 1") must beTrue.eventually
      hookupClient.messages.contains("") must beTrue.eventually

      Logger.debug("WebSocket test finished")
    }

  }

}