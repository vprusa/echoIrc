package scalajsreact.template.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import japgolly.scalajs.react._
import org.scalajs.dom

/**
  * Created by chandrasekharkode on 11/16/14.
  */
object TodoPage {

  val component = ScalaComponent.static("Login")(
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
        "Todo page of echoIrc"
      )
    )
  )
}