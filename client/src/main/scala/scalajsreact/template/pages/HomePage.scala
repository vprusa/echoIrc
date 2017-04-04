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
          ^.color := "#000",
          ^.href := "https://github.com/japgolly/scalajs-react",
          "echoIrc"
        )
      ),

      <.section(
        ^.marginTop := "2.2em",
        ^.fontSize := "115%",
        ^.color := "#333",
        "Homepage of IRC chat over Websocket, bot and admin"
      )
    )
  )
}