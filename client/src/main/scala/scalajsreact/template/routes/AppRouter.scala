package scalajsreact.template.routes

import japgolly.scalajs.react.extra.router.{Resolution, RouterConfigDsl, RouterCtl, _}

import scalajsreact.template.pages.{LogsPage, AdminPage, ErrorPage, HomePage, StatsPage, TodoPage}
import scalajsreact.template.components.TopNav
import scalajsreact.template.components.Footer
import scalajsreact.template.models.{AppConfig, Menu, MenuInner, MenuOutisde}
import org.scalajs.dom
import scalacss.Defaults._
import scalacss.ScalaCssReact._
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._
import vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._

import scala.collection.mutable.ListBuffer
import scalajsreact.template.pages.IrcChatPage
import scalajsreact.template.models.IrcChatProps
import scalajsreact.template.css.GlobalStyle

object AppRouter {

  sealed trait AppPage

  case object Home extends AppPage

  case object Todo extends AppPage

  case object IrcChat extends AppPage

  case object Admin extends AppPage

  case object Logs extends AppPage

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
      | staticRoute("logs", Logs) ~> render(LogsPage.component(AppConfig.ircChatPropsTest))
      | staticRoute(root, IrcChat) ~> render(IrcChatPage.WebSocketsApp(AppConfig.ircChatPropsTest))
      | staticRoute("admin", Admin) ~> render(AdminPage.component())
      | staticRoute("error", Error) ~> render(ErrorPage.component())
      //   | staticRoute("login", Login) ~> redirectToPath("/custom/login")
      //   | staticRoute("logout", Logout) ~> redirectToPath("/custom/logout")
      | itemRoutes
      )
      .notFound(redirectToPage(Error)(Redirect.Replace))
      .renderWith(layout)
  }


  var mainMenuDefault = ListBuffer[Menu](
    MenuInner("Home", Home),
    MenuOutisde("Logout", Logout, "/auth/logout")
  )

  var mainMenuBefore = ListBuffer[Menu](
    MenuInner("Home", Home)
  )

  var mainMenuAfter = ListBuffer[Menu](
    MenuOutisde("Logout", Logout, "/auth/logout")
  )

  var mainMenu = ListBuffer[Menu](
    /* MenuInner("Todo", Todo),
     MenuInner("Stats", Items(MenuItem.Info)),
     // MenuInner("Error", Error),
     MenuInner("IrcChat", IrcChat),
     // MenuInner("Logout", Logout),
     // MenuOutisde("Logout", Logout, "/custom/logout"),
     MenuOutisde("Login", Login, "/custom/login")*/
  )

  def loadReactElementVarData(): Unit = {
    val element = dom.document.getElementById("reactData")

    dom.console.info("element -- " + element.toString)
    System.out.println("element -- " + element.toString)
  }


  def loadFromDom(name: String): upickle.Js.Value = {
    upickle.json.read(dom.document.getElementById(name).textContent)
  }

  def loadMenuFromDom(): Unit = {
    val topMenu: upickle.Js.Value = loadFromDom("reactData")
    org.scalajs.dom.console.log("loadMenuFromDom")
    org.scalajs.dom.console.log(topMenu.toString())

    mainMenu ++= mainMenuBefore

    topMenu.arr.foreach(item => {
      org.scalajs.dom.console.log(item.toString())
      if (item.str.matches("todo")) {
        org.scalajs.dom.console.log(".todo")
        val newMenuItem = MenuInner("Todo", Todo)
        if (!mainMenu.contains(newMenuItem))
          mainMenu += newMenuItem

      } else if (item.str.matches("stats")) {
        org.scalajs.dom.console.log(".stats")
        val newMenuItem = MenuInner("Stats", Items(MenuItem.Info))
        if (!mainMenu.contains(newMenuItem))
          mainMenu += newMenuItem
      } else if (item.str.matches("ircchat")) {
        org.scalajs.dom.console.log(".ircchat")
        val newMenuItem = MenuInner("IrcChat", IrcChat)
        if (!mainMenu.contains(newMenuItem))
          mainMenu += newMenuItem
      }
    })
    mainMenu ++= mainMenuAfter //MenuOutisde("Logout", Logout, "/auth/logout")


    org.scalajs.dom.console.log(mainMenu.toString())
  }


  def layout(c: RouterCtl[AppPage], r: Resolution[AppPage]) = {

    <.div(
      TopNav.component(TopNav.Props(mainMenu, r.page, c)),
      <.div(
        GlobalStyle.content,
        r.render()
      ),
      Footer.component()
    )
  }

  val baseUrl =
    if (dom.window.location.hostname == "localhost") {
      BaseUrl.fromWindowOrigin_/ / "react/"
    } else
      BaseUrl.fromWindowOrigin / "react/"

}
