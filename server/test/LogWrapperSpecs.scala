import java.text.SimpleDateFormat
import java.util.Calendar

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._
import models.LogWrapper
import shared.SharedMessages._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class LogWrapperSpecs extends Specification {

  "Application" should {
    "log test message" in new WithApplication {

      // prepare vars
      val testUniqueId = ("testProvider", "testUserId")
      val testTarget = "testTarget"
      val testMessageStr = "Test message"

      // generate logs content
      val logWrapper = new LogWrapper(testUniqueId, testTarget)
      logWrapper.logLineAndExecuteScriptAction(JsMessage("testSender", testTarget, testMessageStr))

      // search logs
      val result: JsMessageSearchResults = logWrapper.searchLogs(JsMessageSearchLogsRequest(testMessageStr, testTarget))

      // check results
      result.results.length must beGreaterThan(0)
      result.results.foreach(_.jsmsg.msg must contain(testMessageStr))
    }
  }
}
