package scalajsreact.template.pages

import scalacss.Defaults._
import scalacss.ScalaCssReact._
import scalajsreact.template.routes.MenuItem
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Generic.MountedWithRoot
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.ext.Ajax

//import scala.collection.generic.BitOperations.Int
import scala.collection.mutable.ListBuffer
import scalajsreact.template.components.LeftNav
import scalajsreact.template.models.IrcChatProps
import scalacss.internal.Attrs
import scalajsreact.template.pages.IrcChatPage.{Backend, ChatState, defaultTargetStateInside}

//import shared.SharedMessages._

object StatsPage {


  object Style extends StyleSheet.Inline {

    import dsl._

    val container = style(display.flex, minHeight(600.px))

    val nav =
      style(display.block, float.left, width(200.px), borderRight :=! "1px solid rgb(223, 220, 220)")

    val content = style(padding(30.px), display.inlineBlock, width(700.px))
  }

  val component = ScalaComponent
    .builder[Props]("StatsPage")
    .render_P { P =>
      <.div(
        "StatsPage"
      )
    }
    .build

  case class Props(selectedPage: MenuItem, ctrl: RouterCtl[MenuItem], props: IrcChatProps)


  def apply(props: Props) = component(props).vdomElement

}