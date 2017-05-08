import dao.TokenDAO
import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mutable.Specification
import play.Logger
import play.api.Application
import play.api.test.WithApplicationLoader
import securesocial.core._
import securesocial.core.providers.MailToken

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

/** test the kitty cat database */
class TokenDAOSpec extends Specification {

  "TokenDAO" should {
    "work as expected" in new WithApplicationLoader {
      val app2dao = Application.instanceCache[TokenDAO]
      val dao: TokenDAO = app2dao(app)

      val currentDate: DateTime = (new DateTime).withZone(DateTimeZone.UTC)

      MailToken
      val testTokens = Set(
        /* case class MailToken(uuid: String, email: String, creationTime: DateTime, expirationTime: DateTime, isSignUp: Boolean) {
            def isExpired = expirationTime.isBeforeNow
        }*/
        MailToken(
          "testUuid",
          "test@localhost",
          currentDate,
          currentDate.plus(3600),
          false
        )
      )

      Await.result(Future.sequence(testTokens.map(dao.insert)), 1 seconds)
      val storedTokens = Await.result(dao.all(), 1 seconds)
      Logger.debug("DB test")
      Logger.debug(storedTokens.toString())
      storedTokens.toSet must equalTo(testTokens)
    }
  }
}