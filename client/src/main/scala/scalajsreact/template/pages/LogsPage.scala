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

}