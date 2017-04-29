package scalajsreact.template.models

object UserPermissions {

  trait UserPermissions

  trait HomePermission extends UserPermissions

  trait LogoutPermission extends UserPermissions

  trait LoginPermission extends UserPermissions

}

import scalajsreact.template.routes.AppRouter._

/**
  * Created by vprusa on 4/25/17.
  */
case class User(username: String, topNavPermission: List[AppPage]) {}
