package scalajsreact.template.pages

import japgolly.scalajs.react._, vdom.html_<^._

/**
  * Created by chandrasekharkode on 11/16/14.
  */
object HomePage {

  val component = ScalaComponent.static("Home")(
    <.div(
      <.h1(
        <.a(
          ^.color := "#FFFFFF",
//          ^.href := "https://github.com/japgolly/scalajs-react",
          ^.href := "https://github.com/vprusa/echoIrc",
          "echoIrc"
        )
      ),

      <.section(
        ^.marginTop := "2.2em",
        ^.fontSize := "115%",
        ^.color := "#FFFFFF",
        "Homepage of IRC chat over Websocket, bot and admin"
      )
    )
  )
}