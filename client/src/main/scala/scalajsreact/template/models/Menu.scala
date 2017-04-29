package scalajsreact.template.models

import scalajsreact.template.routes.AppRouter.AppPage

sealed trait Menu

case class MenuInner(name: String, route: AppPage) extends Menu

case class MenuOutisde(name: String, route: AppPage, staticRedirect: String) extends Menu