package controllers

import javax.inject.Inject

import play.api.mvc.{Action, AnyContent, RequestHeader}
import play.api.{Configuration, Logger}
import play.filters.csrf.CSRFAddToken
import service.DemoUser
import securesocial.core._
import securesocial.core.services.RoutesService
import securesocial.controllers.BaseLoginPage

class CustomLoginController @Inject() (val csrfAddToken: CSRFAddToken, implicit override val env: RuntimeEnvironment[DemoUser]) extends BaseLoginPage[DemoUser] {
  override def login: Action[AnyContent] = {
    Logger.debug("using CustomLoginController")
    super.login
  }
}

class CustomRoutesService(configuration: Configuration) extends RoutesService.Default(configuration) {
  override def loginPageUrl(implicit req: RequestHeader): String = absoluteUrl(controllers.routes.CustomLoginController.login())
}