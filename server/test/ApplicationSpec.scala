import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "render the login page" in new WithApplication {
      val home = route(FakeRequest(GET, "/custom/login")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      //contentAsString(home) must contain("Your new application is ready.")
    }

  }
}
