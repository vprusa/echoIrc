package scalajsreact.template.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Footer {


  val component = ScalaComponent.static("Footer")(
    <.footer(^.textAlign.center,
      <.div(^.borderBottom := "1px solid grey", ^.padding := "0px"),
      <.p(^.paddingTop := "5px", "Built using scalajs/scalajs-react/scalacss")
    )
  )

}
