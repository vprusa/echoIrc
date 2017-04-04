package scalajsreact.template.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

/**
 * Created by chandrasekharkode on 11/16/14.
 */
object ErrorPage {

  private val p =
    <.p(^.margin := "1.3em 0")

  val component = ScalaComponent.static("Home")(
    <.div("Error page")
  )
}