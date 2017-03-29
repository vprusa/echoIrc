package scalajsreact.template

import japgolly.scalajs.react._
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalajsreact.template.css.AppCSS
import scalajsreact.template.routes.AppRouter

@JSExport
object ReactApp extends JSApp {
  // pallete -
  // http://www.colourlovers.com/palette/338155/Black_and_orange
  // http://www.colourlovers.com/palette/3831583/Orange_and_gray
  // http://www.colourlovers.com/palette/2476685/Black_And_Orange
  // http://www.colourlovers.com/palette/371133/ok_I_admit
  // http://www.colourlovers.com/palette/1480370/upbeat!
  @JSExport
  override def main(): Unit = {
    AppCSS.load
    AppRouter.router().render(dom.document.body)
  }

}

