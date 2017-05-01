package scalajsreact.template.components.items

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object SearchLogs {

  val component =
    ScalaComponent.builder.static("SearchLogs")(<.div(
      <.h3("Searching logs")
      // get list names and show then as 2 links  (outside and render into page)
      // foreach onclick load log content into div
      // add input button for regex search request on logs
      // - server will have to load and parse log files -> get matching patterns -> add html spans/divs to them with appropriate classes
      // -- classes will be set here and used in example tag

    )).build

  def apply() = component().vdomElement
}