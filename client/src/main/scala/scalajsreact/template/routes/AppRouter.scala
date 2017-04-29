package scalajsreact.template.routes

import japgolly.scalajs.react.extra.router.{Resolution, RouterConfigDsl, RouterCtl, _}

import scalajsreact.template.pages.{AdminPage, ErrorPage, HomePage, StatsPage, TodoPage}
import scalajsreact.template.components.TopNav
import scalajsreact.template.components.Footer
import scalajsreact.template.models.{AppConfig, Menu, MenuInner, MenuOutisde}
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

  case object Todo extends AppPage

  case object IrcChat extends AppPage

  case object Admin extends AppPage

  //case object Stats extends AppPage

  case object Logout extends AppPage

  case object Login extends AppPage

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
      | staticRoute("todo", Todo) ~> render(TodoPage.component())
      | staticRoute(root, IrcChat) ~> render(IrcChatPage.WebSocketsApp(AppConfig.ircChatPropsTest))
      | staticRoute("admin", Admin) ~> render(AdminPage.WebSocketsApp(AppConfig.ircChatPropsTest))
      | staticRoute("error", Error) ~> render(ErrorPage.component())
      //   | staticRoute("login", Login) ~> redirectToPath("/custom/login")
      //   | staticRoute("logout", Logout) ~> redirectToPath("/custom/logout")
      | itemRoutes
      )
      .notFound(redirectToPage(Error)(Redirect.Replace))
      .renderWith(layout)
  }

  var mainMenu = List(
    MenuInner("Home", Home),
    MenuInner("Todo", Todo),
    MenuInner("Stats", Items(MenuItem.Info)),
    // MenuInner("Error", Error),
    MenuInner("IrcChat", IrcChat),
    // MenuInner("Logout", Logout),
    // MenuOutisde("Logout", Logout, "/custom/logout"),
    MenuOutisde("Logout", Logout, "/auth/logout"),
    MenuOutisde("Login", Login, "/custom/login")

  )

  def loadReactElementVarData(): Unit = {
    val element = dom.document.getElementById("reactData")

    dom.console.info("element -- " + element.toString)
    System.out.println("element -- " + element.toString)
  }

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
