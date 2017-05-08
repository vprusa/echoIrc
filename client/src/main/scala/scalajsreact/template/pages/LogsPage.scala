package scalajsreact.template.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Generic.MountedWithRoot
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.ext.Ajax

import scalajsreact.template.components.SearchLogs
import scalajsreact.template.models.IrcChatProps
import scalajsreact.template.pages.LogsStatsPage.Style.style

/**
  * Created by chandrasekharkode on 11/16/14.
  */
object LogsPage {

  val component = SearchLogs.searchLogsComponent

  val component2 = ScalaComponent.static("Login")(
    <.div(
      <.h1(
        <.a(
          ^.color := "#FFFFFF",
          // ^.href := "https://github.com/japgolly/scalajs-react",
          ^.href := "https://github.com/vprusa/echoIrc",
          "echoIrc"
        )
      ),

      <.section(
        ^.marginTop := "2.2em",
        ^.fontSize := "115%",
        ^.color := "#FFFFFF",
        "Admin page of echoIrc - TODO"
      )
    )
  )


}