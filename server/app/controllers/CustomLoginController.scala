package controllers

import javax.inject.Inject

import play.api.mvc.{Action, AnyContent, RequestHeader}
import play.api.{Configuration, Logger}
import play.filters.csrf.CSRFAddToken
import securesocial.controllers.{BaseLoginPage, ProviderControllerHelper}
import securesocial.core.SecureSocial
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.RoutesService
import service.MyEnvironment

class CustomLoginController @Inject()(val csrfAddToken: CSRFAddToken, implicit override val env: MyEnvironment) extends BaseLoginPage {

  override def login: Action[AnyContent] = csrfAddToken {
    UserAwareAction {
      implicit request => {
        Logger.debug("using CustomLoginController")
        // from BaseLoginPageui
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
