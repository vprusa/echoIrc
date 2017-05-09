import dao.UserDAO
import org.specs2.mutable.Specification
import play.Logger
import play.api.Application
import play.api.test.WithApplicationLoader
import securesocial.core._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

/** test the kitty cat database */
class SecureSocialSpec extends Specification {

  "customLogin" should {
    "be authenticated" in new WithApplicationLoader {
      val app2dao = Application.instanceCache[UserDAO]
      val dao: UserDAO = app2dao(app)

      val testUsers = Set(
        /*BasicProfile(
          providerId: String,
          userId: String,
          firstName: Option[String],
          lastName: Option[String],
          fullName: Option[String],
          email: Option[String],
          avatarUrl: Option[String],
          authMethod: AuthenticationMethod,
          oAuth1Info: Option[OAuth1Info] = None,
        oAuth2Info: Option[OAuth2Info] = None,
      passwordInfo: Option[PasswordInfo] = None
        )*/
        BasicProfile(
          "testProvider",
          "userId",
          Some(""),
          Some(""),
          Some(""),
          Some("test@localhost"),
          None,
          AuthenticationMethod.UserPassword,
          None,
          None,
          None//Some(PasswordInfo("hasher", "userpass", None))
        )
      )

      Await.result(Future.sequence(testUsers.map(dao.insert)), 1 seconds)
      val storedUsers = Await.result(dao.all(), 1 seconds)
      Logger.debug("DB test")
      Logger.debug(storedUsers.toString())
      storedUsers.toSet must equalTo(testUsers)


    }
  }
}