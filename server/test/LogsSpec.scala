import models.LogsBase
import org.junit.runner._
import org.specs2.runner._
import play.api.Logger
import play.api.test._
import shared.SharedMessages.JsMessageSearchLogsRequest

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class LogsSpec extends PlaySpecification {

  "logs" should {
    "find something" in new WithApplication {
      Logger.info("find something")
      val logs: LogsBase = new LogsBase(("27739871", "github"))
      val jsmsg: JsMessageSearchLogsRequest = JsMessageSearchLogsRequest(regex = """JsMessage\(.*""", target = "*")

      val ret = logs.searchLogs(jsmsg)
      Logger.info(ret.toString)
    }

  }
}
