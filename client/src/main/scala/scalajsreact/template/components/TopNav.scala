package scalajsreact.template.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.extra.router.RouterCtl

import scala.scalajs.js
import scalacss.Defaults._
import scalacss.ScalaCssReact._

import scalajsreact.template.models.Menu
import scalajsreact.template.routes.AppRouter.AppPage

import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._
import vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._

import scalacss.ScalatagsCss._


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

  case class Props(menus: Vector[Menu], selectedPage: AppPage, ctrl: RouterCtl[AppPage])

  implicit val currentPageReuse = Reusability.by_==[AppPage]
  implicit val propsReuse = Reusability.by((_: Props).selectedPage)

  val component = ScalaComponent.builder[Props]("TopNav")
    .render_P { P =>
      <.header(
        <.nav(
          <.ul(
            P.menus.map(
              item => <.li(^.key := item.name,
                //Style.menuItem(
                //  item.route.getClass == P.selectedPage.getClass
                //),
                item.name,
                P.ctrl setOnClick item.route
              )
            ).toTagMod

            //Style.navMenu,
            //P.menus.map(
            //  item => <.li(^.key := item.name,
            //    Style.menuItem(
            //      item.route.getClass == P.selectedPage.getClass
            //    ),
            //    item.name,
            //    P.ctrl setOnClick item.route
            //  )
            // )
          )
        )
      )
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

  //def apply(props: Props, ref: js.UndefOr[String] = "", key: js.Any = {}) = component.set(key, ref)(props)


  /*
    val component = ScalaComponent.builder[Props]("TopNav")
      .render_P { P =>
        <.header(
          <.nav(
            <.ul(
              //Style.navMenu,
             // P.menus.map(item => <.li(^.key := item.name,
                //Style.menuItem(
                //  item.route.getClass == P.selectedPage.getClass),
               //   item.name,
               //   P.ctrl setOnClick item.route)
              )
            )
          )
        )
      }
      .configure(Reusability.shouldComponentUpdate)
      .build
  */
  // def apply(props: Props, ref: js.UndefOr[String] = "", key: js.Any = {}) = component.set(key, ref)(props)

  /*
    case class Props(menus: Vector[Menu], selectedPage: AppPage, ctrl: RouterCtl[AppPage])

    implicit val currentPageReuse = Reusability.by_==[AppPage]
    implicit val propsReuse = Reusability.by((_:Props).selectedPage)

    val component = ReactComponentB[Props]("TopNav")
      .render_P { P =>
        <.header(
          <.nav(
            <.ul(Style.navMenu,
              P.menus.map(item => <.li(^.key := item.name,
                Style.menuItem(
                  item.route.getClass == P.selectedPage.getClass),
                item.name,
                P.ctrl setOnClick item.route)))
          )
        )
      }
      .configure(Reusability.shouldComponentUpdate)
      .build

    def apply(props: Props, ref: js.UndefOr[String] = "", key: js.Any = {}) = component.set(key, ref)(props)
  */
}


