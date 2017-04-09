package scalajsreact.template.routes

import japgolly.scalajs.react.extra.router.{Resolution, RouterConfigDsl, RouterCtl, _}

import scalajsreact.template.pages.{ErrorPage, HomePage}
import scalajsreact.template.components.TopNav
import scalajsreact.template.components.Footer
import scalajsreact.template.models.{IrcChatProps, Menu}
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

  case object IrcChat extends AppPage

  //case class IrcChat(username: String) extends AppPage

  case object Error extends AppPage

  val routerConfig = RouterConfigDsl[AppPage].buildConfig { dsl =>
    import dsl._

    (trimSlashes
      | staticRoute("home", Home) ~> render(HomePage.component())
      | staticRoute(root, IrcChat) ~> render(IrcChatPage.WebSocketsApp(IrcChatProps(username = "UserBot", url = "ws://localhost:9000/chat")))
      //| dynamicRouteCT(root ~ string("[a-z]+").caseClass[IrcChat]) ~> render(IrcChatPage.WebSocketsApp("asdasd"))
      | staticRoute("error", Error) ~> render(ErrorPage.component())
      )
      .notFound(redirectToPage(Error)(Redirect.Replace))
      .renderWith(layout)
    //.verify(Home, Error)
  }

  val mainMenu = Vector(
    Menu("Home", Home),
    Menu("Error", Error),
    Menu("IrcChat", IrcChat)
    //, Menu("Items", Items(Item.Info))
  )


  def layout(c: RouterCtl[AppPage], r: Resolution[AppPage]) = {
    <.div(
      TopNav.component(TopNav.Props(mainMenu, r.page, c)),
      r.render(),
      Footer.component()
    )
  }

  /*
  def layout(c: RouterCtl[Page], r: Resolution[Page]) =
    <.div(
      navMenu(c),
      <.div(^.cls := "container", r.render()))


  val navMenu = ScalaComponent.builder[RouterCtl[AppPage]]("Menu")
    .render_P { ctl =>

      def nav(name: String, target: AppPage) =
        <.li(
          ^.cls := "navbar-brand active",
          ctl setOnClick target,
          name)

      <.div(
        ^.cls := "navbar navbar-default",
        <.ul(
          ^.cls := "navbar-header",
          nav("Home", Home)
        )
      )
    }
    .configure(Reusability.shouldComponentUpdate)
    .build
*/

  val baseUrl =
    if (dom.window.location.hostname == "localhost") {
      BaseUrl.fromWindowOrigin_/ / "react/"
      // BaseUrl.fromWindowOrigin / "react/"
    } else
      BaseUrl.fromWindowOrigin / "react/"


}
