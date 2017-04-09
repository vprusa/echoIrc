package scalajsreact.template.components

import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._
import vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._



import scalajsreact.template.components.TopNav.Props //.prefix_<^._

object Footer {


  val component = ScalaComponent.static("Footer")(
    <.footer(^.textAlign.center,
      <.div(^.borderBottom := "1px solid grey", ^.padding := "0px"),
      <.p(^.paddingTop := "5px", "Built using scalajs/scalajs-react/scalacss")
    )
  )

}
