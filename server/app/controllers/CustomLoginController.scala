package controllers

import javax.inject.Inject

import securesocial.controllers.{BaseLoginPage, ProviderControllerHelper}
import play.api.mvc.{Action, AnyContent, RequestHeader}
import play.api.{Configuration, Logger}
import play.filters.csrf.CSRFAddToken
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.{IdentityProvider, RuntimeEnvironment, SecureSocial}
import securesocial.core.services.RoutesService
import service.MyEnvironment

import scala.collection.Searching.search

class CustomLoginController @Inject()(val csrfAddToken: CSRFAddToken, implicit override val env: MyEnvironment) extends BaseLoginPage {

  override def login: Action[AnyContent] = csrfAddToken {
    UserAwareAction {
      implicit request => {
        Logger.debug("using CustomLoginController")
        Logger.debug(request.toString)
        Logger.debug(request.request.toString)
        Logger.debug(request.request.body.toString)
        Logger.debug(request.request.body.asFormUrlEncoded.toString)
        Logger.debug(request.request.body.asJson.toString)
        Logger.debug(request.request.toString)
        Logger.debug(request.request.toString)
        Logger.debug(request.request.toString)
        Logger.debug(request.user.toString)
        Logger.debug(request.authenticator.toString)
        // from BaseLoginPage
        if (request.user.isDefined) {
          // if the user is already logged in, a referer is set and we handle the
          // referer the same way as an OriginalUrl in the session, we redirect back
          // to this URL. Otherwise, just redirect to the application's landing page
          val to = (if (SecureSocial.enableRefererAsOriginalUrl) {
            SecureSocial.refererPathAndQuery
          } else {
            None
          }).getOrElse(ProviderControllerHelper.landingUrl(configuration))
          Logger.debug("User already logged in, skipping login page. Redirecting to %s".format(to))
          Redirect(to)
        } else {
          if (SecureSocial.enableRefererAsOriginalUrl) {
            SecureSocial.withRefererAsOriginalUrl(Ok(env.viewTemplates.getLoginPage(UsernamePasswordProvider.loginForm)))
          } else {
            Ok(env.viewTemplates.getLoginPage(UsernamePasswordProvider.loginForm))
          }
        }
      }
    }
  }
}

class CustomRoutesService(configuration: Configuration) extends RoutesService.Default(configuration) {
  override def loginPageUrl(implicit req: RequestHeader): String = absoluteUrl(controllers.routes.CustomLoginController.login())
}

/*
import javax.inject.Inject

import securesocial.controllers.BaseLoginPage
import play.api.mvc.{Action, AnyContent, RequestHeader}
import play.api.{Configuration, Logger}
import play.filters.csrf.CSRFAddToken
import securesocial.core.services.RoutesService
import service.MyEnvironment


import securesocial.core._
import securesocial.core.utils._
import play.api.{ Configuration, Play }
import providers.UsernamePasswordProvider
import play.api.mvc._
import play.filters.csrf._

class CustomLoginController @Inject()(val csrfAddToken: CSRFAddToken, implicit override val env: MyEnvironment) extends BaseLoginPage {
override def login: Action[AnyContent] = {
  Logger.debug("using CustomLoginController")
  super.login
}
}

class CustomRoutesService(configuration: Configuration) extends RoutesService.Default(configuration) {
override def loginPageUrl(implicit req: RequestHeader): String = absoluteUrl(controllers.routes.CustomLoginController.login())
}
*/
/*
import securesocial.controllers.BaseLoginPage
import play.api.mvc.{ RequestHeader, AnyContent, Action }
import play.api.Logger
import securesocial.core.{ RuntimeEnvironment, IdentityProvider }
import models.User
import securesocial.core.services.RoutesService

class CustomLoginController(implicit override val env: RuntimeEnvironment[User]) extends BaseLoginPage[User] {
override def login: Action[AnyContent] = {
  Logger.debug("Using CustomLoginController")
  super.login
}
}

class CustomRoutesService extends RoutesService.Default {
override def loginPageUrl(implicit req: RequestHeader): String =
  routes.CustomLoginController.login().absoluteURL(IdentityProvider.sslEnabled)
}
*/