import java.net.URI

import io.backchat.hookup._
import org.specs2.mutable._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.test._
import shared.SharedMessages.{JsMessageBase, JsMessageStarBotRequest}

import scala.collection.mutable.ListBuffer

class RestSpec extends Specification {

  "Application" should {

    // https://stackoverflow.com/questions/19306787/writing-a-unit-test-for-play-websockets/37209708#37209708

    "connect to websocket" in new WithServer(port = 9000) {
      Logger.debug("Test websocket")
      val hookupClient = new DefaultHookupClient(HookupClientConfig(URI.create("ws://localhost:9000/chat?botName=testName"))) {
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
            val jsval = upickle.default.write[JsMessageBase](JsMessageStarBotRequest("testName", Array.empty[String]))
            Logger.debug(jsval.toString())
            send(jsval)
            // send("Hello Server")
          }
        }
      }
      Logger.debug(hookupClient.toString)
      Logger.debug(hookupClient.messages.toString)
      //hookupClient.messages.contains("Hello Client") must beTrue.eventually
    }

  }

}