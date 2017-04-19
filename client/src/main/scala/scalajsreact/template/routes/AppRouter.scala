package scalajsreact.template.routes

import japgolly.scalajs.react.extra.router.{Resolution, RouterConfigDsl, RouterCtl, _}

import scalajsreact.template.pages.{ErrorPage, HomePage, LoginPage, TodoPage}
import scalajsreact.template.components.TopNav
import scalajsreact.template.components.Footer
import scalajsreact.template.models.{AppConfig, Menu}
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._
import vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._

import scalajsreact.template.pages.IrcChatPage


object AppRouter {

  sealed trait AppPage

  case object Home extends AppPage

  case object Login extends AppPage

  case object Todo extends AppPage

  case object IrcChat extends AppPage

  //case class IrcChat(username: String) extends AppPage
  case class Items(p: MenuItem) extends AppPage

  case object Error extends AppPage

  val routerConfig = RouterConfigDsl[AppPage].buildConfig { dsl =>
    import dsl._

    val itemRoutes: Rule =
      MenuItem.routes.prefixPath_/("#items").pmap[AppPage](Items) {
        case Items(p) => p
      }
    (trimSlashes
      | staticRoute("home", Home) ~> render(HomePage.component())
      | staticRoute("login", Login) ~> render(LoginPage.component())
      | staticRoute("todo", Todo) ~> render(TodoPage.component())
      | staticRoute(root, IrcChat) ~> render(IrcChatPage.WebSocketsApp(AppConfig.ircChatProps))
      | staticRoute("error", Error) ~> render(ErrorPage.component())
      | itemRoutes
      )
      .notFound(redirectToPage(Error)(Redirect.Replace))
      .renderWith(layout)
  }

  val mainMenu = Vector(
    Menu("Home", Home),
    Menu("Login", Login),
    Menu("Todo", Todo),
    Menu("Error", Error),
    Menu("IrcChat", IrcChat),
    Menu("Stats", IrcChat)
  )

  def layout(c: RouterCtl[AppPage], r: Resolution[AppPage]) = {
    <.div(
      TopNav.component(TopNav.Props(mainMenu, r.page, c)),
      r.render(),
      Footer.component()
    )
  }

  val baseUrl =
    if (dom.window.location.hostname == "localhost") {
      BaseUrl.fromWindowOrigin_/ / "react/"
    } else
      BaseUrl.fromWindowOrigin / "react/"

}
