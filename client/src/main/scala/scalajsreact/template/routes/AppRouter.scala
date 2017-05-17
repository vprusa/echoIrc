package scalajsreact.template.routes

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterConfigDsl, RouterCtl, _}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

import scala.collection.mutable.ListBuffer
import scalacss.ScalaCssReact._
import scalajsreact.template.components.{Footer, TopNav}
import scalajsreact.template.css.GlobalStyle
import scalajsreact.template.models.{AppConfig, Menu, MenuInner, MenuOutisde}
import scalajsreact.template.pages.{ErrorPage, HomePage, IrcChatPage, LogsPage}

object AppRouter {

  sealed trait AppPage

  case object Home extends AppPage

  case object IrcChat extends AppPage

  case object Logs extends AppPage

  case object Logout extends AppPage

  case object Login extends AppPage

  case object Error extends AppPage

  val routerConfig = RouterConfigDsl[AppPage].buildConfig { dsl =>
    import dsl._

       (trimSlashes
      | staticRoute("home", Home) ~> render(HomePage.component())
      | staticRoute("logs", Logs) ~> render(LogsPage.component(AppConfig.ircChatPropsTest))
      | staticRoute(root, IrcChat) ~> render(IrcChatPage.WebSocketsApp(AppConfig.ircChatPropsTest))
      | staticRoute("error", Error) ~> render(ErrorPage.component())
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
    val data: upickle.Js.Value = loadFromDom("reactData")
    val topMenu: upickle.Js.Value = data.obj.get("topMenuList").getOrElse(null)
    // TODO should not happen
    if (topMenu == null)
      return

    org.scalajs.dom.console.log("loadMenuFromDom")
    org.scalajs.dom.console.log(topMenu.toString())

    mainMenu ++= mainMenuBefore

    topMenu.arr.foreach(item => {
      org.scalajs.dom.console.log(item.toString())
      if (item.str.matches("logs")) {
        org.scalajs.dom.console.log(".logs")
        val newMenuItem = MenuInner("Logs", Logs)
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
