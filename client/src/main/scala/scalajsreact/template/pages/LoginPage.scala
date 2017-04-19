package scalajsreact.template.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

/**
  * Created by chandrasekharkode on 11/16/14.
  */
object LoginPage {

  val loginForm = ScalaComponent.static("LoginForm")(
    <.div(

    )
  )

  val component = ScalaComponent.static("Login")(
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
