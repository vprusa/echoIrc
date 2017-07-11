import org.junit.runner.RunWith
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import play.api.data.Form
import play.api.i18n.Lang
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.test._
import play.twirl.api.Html
import securesocial.controllers.ViewTemplates
import securesocial.core.AuthenticationResult.Authenticated
import securesocial.core.providers._
import securesocial.core.providers.utils.PasswordHasher
import securesocial.core.services._
import securesocial.core.{AuthenticationResult, BasicProfile, PasswordInfo}

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class AuthenticateOwnerSpec extends PlaySpecification with Mockito {
  "AuthenticateOwnerSpec" should {

    "Authenticate owner" in new WithOwnerMocks {
      val form = FakeRequest().withFormUrlEncodedBody("username" -> "owner", "password" -> "password")
      val resFut = upp.authenticate()(form)
      await(resFut) match {
        case Authenticated(_) => {
          success
        }
        case t => failure(t.toString)
      }
    }
  }

  trait WithOwnerMocks extends Before with Mockito with MustThrownExpectations {
    val userService = mock[UserService[User]]
    val avatarService = mock[AvatarService]
    val viewTemplates = mock[ViewTemplates]
    val passwordHashers = mock[Map[String, PasswordHasher]]
    val upp = new UsernamePasswordProvider(userService, Some(avatarService), viewTemplates, passwordHashers)

    def before = {
      viewTemplates.getLoginPage(any[Form[(String, String)]], any[Option[String]])(any[RequestHeader], any[Lang]) returns Html("login page")
      userService.find(upp.id, "owner") returns Future(Some(basicProfileFor(User("owner", "password"))))
      passwordHashers.get("bcrypt") returns Some(new PasswordHasher.Default(12))
      avatarService.urlFor("owner") returns Future(None)
    }

    def basicProfileFor(user: User) = BasicProfile(
      providerId = upp.id,
      userId = user.email,
      firstName = None,
      lastName = None,
      fullName = None,
      email = Some(user.email),
      avatarUrl = None,
      authMethod = upp.authMethod,
      oAuth1Info = None,
      oAuth2Info = None,
      passwordInfo = Some(PasswordInfo("bcrypt", user.hash))
    )
  }

  case class User(email: String, password: String) {
    import org.mindrot.jbcrypt.BCrypt

    val hash = BCrypt.hashpw(password, BCrypt.gensalt(12))
  }


}
