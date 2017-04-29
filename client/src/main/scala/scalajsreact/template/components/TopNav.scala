package scalajsreact.template.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.extra.router.RouterCtl

import scala.scalajs.js
import scalacss.Defaults._
import scalacss.ScalaCssReact._
import scalajsreact.template.models.{Menu, MenuInner, MenuOutisde}
import scalajsreact.template.routes.AppRouter.{AppPage, mainMenu}
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._
import vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._
import jdk.nashorn.internal.parser.JSONParser

//import scalacss.ScalatagsCss._


object TopNav {

  object Style extends StyleSheet.Inline {

    import dsl._

    val navMenu = style(display.flex,
      alignItems.center,
      backgroundColor(c"#C33D24"),
      margin.`0`,
      listStyle := "none")

    val menuItem = styleF.bool(selected => styleS(
      padding(20.px),
      fontSize(1.5.em),
      cursor.pointer,
      color(c"#FFFFFF"),
      mixinIfElse(selected)(
        backgroundColor(c"#FD4507"),
        fontWeight._500)
      (&.hover(backgroundColor(c"#FF6203")))
    ))

  }

  import upickle.default._
  import scalajsreact.template.models.User
  import scalajsreact.template.models.UserPermissions
  import scalajsreact.template.routes.AppRouter

  def getCurrentUser(): User = {

    // anonym user with no permission
    User("anonym", List(AppRouter.Home, AppRouter.Logout, AppRouter.Login))
  }

  def hasUserPermissionForMenuItem(item: Menu): Boolean = {
    // TODO rest call
    val user: User = getCurrentUser
    user.topNavPermission.contains(item)
  }

  case class Props(menus: List[Menu], selectedPage: AppPage, ctrl: RouterCtl[AppPage])

  implicit val currentPageReuse = Reusability.by_==[AppPage]
  implicit val propsReuse = Reusability.by((_: Props).selectedPage)

  val component = ScalaComponent.builder[Props]("TopNav")
    .render_P { P =>
      <.header(
        <.nav(
          <.ul(
            Style.navMenu,
            P.menus.map(
              unknownItem => {
                unknownItem match {
                  case item: MenuInner => {

                    if (hasUserPermissionForMenuItem(item)) {
                      <.li(^.display := "none")
                      //""
                    } else {
                      <.li(^.key := item.name,
                        Style.menuItem(
                          item.route.getClass == P.selectedPage.getClass
                        ),
                        item.name,
                        P.ctrl setOnClick item.route
                      )
                    }

                  }
                  case item: MenuOutisde => {
                    if (hasUserPermissionForMenuItem(item)) {
                      <.li(^.display := "none")
                      //""
                    } else {
                      <.li(^.key := item.name,
                        Style.menuItem(
                          item.route.getClass == P.selectedPage.getClass
                        ),
                        <.a(
                          ^.href := item.staticRedirect,
                          item.name
                        )
                      )
                    }
                  }
                }

              }
            ).toTagMod
          )
        )
      )
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

}


