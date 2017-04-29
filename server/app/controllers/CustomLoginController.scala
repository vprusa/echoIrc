package controllers

import javax.inject.Inject

import securesocial.controllers.BaseLoginPage
import play.api.mvc.{Action, AnyContent, RequestHeader}
import play.api.{Configuration, Logger}
import play.filters.csrf.CSRFAddToken
import securesocial.core.services.RoutesService
import service.MyEnvironment

class CustomLoginController @Inject()(val csrfAddToken: CSRFAddToken, implicit override val env: MyEnvironment) extends BaseLoginPage {
  override def login: Action[AnyContent] = {
    Logger.debug("using CustomLoginController")
    super.login
  }
}

class CustomRoutesService(configuration: Configuration) extends RoutesService.Default(configuration) {
  override def loginPageUrl(implicit req: RequestHeader): String = absoluteUrl(controllers.routes.CustomLoginController.login())
}

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