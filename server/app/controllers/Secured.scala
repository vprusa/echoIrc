package controllers

import models.User
import play.api.mvc.Results
import play.mvc.Http
import play.mvc.Result
import play.mvc.Security

class Secured extends Security.Authenticator {
  override def getUsername(ctx: Http.Context): String = {
    val authTokenHeaderValues = ctx.request.headers.get(SecurityController.AUTH_TOKEN_HEADER)
    if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues(0) != null)) {
      val user = models.User.findByAuthToken(authTokenHeaderValues(0))
      if (user != null) {
        ctx.args.put("user", user)
        return user.getEmailAddress
      }
    }
    null
  }

  override def onUnauthorized(ctx: Http.Context): Result = play.mvc.Results.unauthorized()
}