package scalajsreact.template.pages

import scalacss.Defaults._
import scalacss.ScalaCssReact._
import scalajsreact.template.routes.MenuItem
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import scalajsreact.template.components.LeftNav

import scalacss.internal.Attrs

object StatsPage {

  object Style extends StyleSheet.Inline {
    import dsl._
    val container = style(display.flex, minHeight(600.px))

    val nav =
      style(display.block, float.left,width(200.px), borderRight :=! "1px solid rgb(223, 220, 220)")

    val content = style(padding(30.px), display.inlineBlock, width(700.px))
  }

  val component = ScalaComponent
    .builder[Props]("ItemsPage")
    .render_P { P =>
      <.div(
        Style.container,
        <.div(Style.nav,
              LeftNav(LeftNav.Props(MenuItem.menu, P.selectedPage, P.ctrl))),
        <.div(Style.content, P.selectedPage.render())
      )
    }
    .build

  case class Props(selectedPage: MenuItem, ctrl: RouterCtl[MenuItem])

  def apply(props: Props) = component(props).vdomElement

}