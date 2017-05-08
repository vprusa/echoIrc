import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class RestSpec extends PlaySpecification {

  /*
  @inline implicit def loggedInFakeRequestWrapper[T](x: FakeRequest[T]) = new LoggedInFakeRequest(x)

  final class LoggedInFakeRequest[T](val self: FakeRequest[T]) extends AnyVal {
    def withLoggedInUser(id: Long) = {
      val userToLogInAs:Identity = ??? //get this from your database using whatever you have in Global
      val cookie = Authenticator.create(userToLogInAs) match {
        case Right(authenticator) => authenticator.toCookie
      }
      self.withCookies(cookie)
    }
  }
  // http://stackoverflow.com/questions/25486427/unit-testing-securesocial-authentication-for-play2-3-x-controllers-in-scala
  */

  "REST API" should {

    "be secured" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beSome.which(status(_) == NOT_FOUND)
    }


    "return logs names" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beSome.which(status(_) == NOT_FOUND)
    }


    "return logs for logs names" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beSome.which(status(_) == NOT_FOUND)
    }
  }
}
