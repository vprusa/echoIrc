package scalajsreact.template.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Generic.MountedWithRoot
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.ext.Ajax

import scalacss.Defaults._
import scalajsreact.template.routes.MenuItem

//import scala.collection.generic.BitOperations.Int
import scalajsreact.template.models.IrcChatProps

//import shared.SharedMessages._

object LogsStatsPage {


  object Style extends StyleSheet.Inline {

    import dsl._

    val container = style(display.flex, minHeight(600.px))

    val nav =
      style(display.block, float.left, width(200.px), borderRight :=! "1px solid rgb(223, 220, 220)")

    val content = style(padding(30.px), display.inlineBlock, width(700.px))
  }

  val component = ScalaComponent
    .builder[IrcChatProps]("StatsPage")
    .render_P { P =>
      <.div(
        "StatsPage",
        statsComponent(P)
      )
    }
    .build

  case class LogsStatsState(wordCount: java.lang.Integer)

  class LogsStatsBackend($: BackendScope[IrcChatProps, LogsStatsState]) {

    var direct: MountedWithRoot[CallbackTo, IrcChatProps, LogsStatsState, IrcChatProps, LogsStatsState] = this.getDirect()

    // to get $ loaded with Everything
    def getDirect(): MountedWithRoot[CallbackTo, IrcChatProps, LogsStatsState, IrcChatProps, LogsStatsState] = {
      $.withEffectsImpure.asInstanceOf[MountedWithRoot[CallbackTo, IrcChatProps, LogsStatsState, IrcChatProps, LogsStatsState]]
    }

    def render(props: IrcChatProps, s: LogsStatsState) = {
      // TODO check selected target
      <.div("render")
    }

    def didMount = {
      import scala.concurrent.ExecutionContext.Implicits.global
      // import scala.concurrent.ExecutionContext.Implicits.global
      Ajax.get(s"/rest/logsStats/").foreach {
        xhr => {
          org.scalajs.dom.console.log(s"callOpenLogFile ${xhr.responseText}")
          $.modState(_.copy())
        }
      }
      $.modState(_.copy())
    }
  }

  val statsComponent = ScalaComponent.builder[IrcChatProps]("Stats")
    //    .initialState(getPersistentChatState())
    .initialState(LogsStatsState(wordCount = new java.lang.Integer(0)))
    .renderBackend[LogsStatsBackend]
    .componentDidMount(_.backend.didMount)
    //.componentWillUnmount(_.backend.end)
    .build


}