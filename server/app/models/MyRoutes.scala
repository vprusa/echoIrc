package models

import javax.inject.Inject

import securesocial.Routes

@Inject
class MyRoutes(errorHandler: play.api.http.HttpErrorHandler,
               LoginPage_2: javax.inject.Provider[securesocial.controllers.LoginPage],
               Registration_5: javax.inject.Provider[securesocial.controllers.Registration],
               PasswordReset_6: javax.inject.Provider[securesocial.controllers.PasswordReset],
               PasswordChange_3: javax.inject.Provider[securesocial.controllers.PasswordChange],
               ProviderController_4: javax.inject.Provider[securesocial.controllers.ProviderController],
               LoginApi_1: javax.inject.Provider[securesocial.controllers.LoginApi],
               Assets_0: javax.inject.Provider[securesocial.controllers.Assets],
               prefix: scala.Predef.String)
  extends Routes(errorHandler, LoginPage_2, Registration_5, PasswordReset_6, PasswordChange_3, ProviderController_4, LoginApi_1, Assets_0, prefix) {

}
