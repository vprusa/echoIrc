package controllers

import com.fasterxml.jackson.databind.node.ObjectNode
import models.User
import play.data.Form
import play.data.FormFactory
import play.data.validation.Constraints
import play.libs.Json
import play.mvc.Controller
import play.mvc.Http
import play.mvc.Result
import play.mvc.Security
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.i18n.{I18nSupport, MessagesApi}

import scala.concurrent.ExecutionContext

object SecurityController {
  val AUTH_TOKEN_HEADER = "X-AUTH-TOKEN"
  val AUTH_TOKEN = "authToken"

  def getUser: User = Http.Context.current.get().args.get("user").asInstanceOf[User]

  class Login {
    @Constraints.Required
    @Constraints.Email var emailAddress = null
    @Constraints.Required var password = null
  }

}

//class SecurityController extends Controller {
@Singleton
class SecurityController @Inject()(implicit actorSystem: ActorSystem, webJarAssets: WebJarAssets,
                                   mat: Materializer,
                                   executionContext: ExecutionContext, override val messagesApi: MessagesApi)
  extends BaseController with I18nSupport {

  @Inject private[controllers] var formFactory: FormFactory = null

  // returns an authToken
  def index2: Result = {
    play.mvc.Results.ok(views.html.index2("msg"))
  }

  // returns an authToken
  def login: Result = {
    val loginForm = formFactory.form(classOf[SecurityController.Login]).bindFromRequest()
    if (loginForm.hasErrors) return play.mvc.Results.badRequest(loginForm.errorsAsJson)
    val login = loginForm.get
    val user = User.findByEmailAddressAndPassword(login.emailAddress, login.password)
    if (user == null) play.mvc.Results.unauthorized()
    else {
      val authToken = user.createToken
      val authTokenJson = Json.newObject
      authTokenJson.put(SecurityController.AUTH_TOKEN, authToken)
      Http.Context.current.get().response.setCookie(Http.Cookie.builder(SecurityController.AUTH_TOKEN, authToken).withSecure(Http.Context.current.get().request.secure).build)
      play.mvc.Results.ok(authTokenJson)
    }
  }

  @Security.Authenticated(classOf[Secured]) def logout: Result = {
    Http.Context.current.get().response.discardCookie(SecurityController.AUTH_TOKEN)
    SecurityController.getUser.deleteAuthToken
    play.mvc.Results.redirect("/")
  }
}