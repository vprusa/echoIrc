package scalajsreact.template

/*
import japgolly.scalajs.react._
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalajsreact.template.css.AppCSS
import scalajsreact.template.routes.AppRouter


import org.scalajs.dom
import japgolly.scalajs.react._, ScalazReact._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._
import pages._

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

    dom.console.info("Router logging is enabled. Enjoy!")
    val router = Router(baseUrl, routerConfig.logToConsole)
    router() renderIntoDOM dom.document.body

   // AppRouter.router().render(dom.document.body)
  }

}
*/


import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._
import vdom.html_<^._

import scalacss.ScalaCssReact._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._

import scalacss.ScalatagsCss._
import pages._
import scalajsreact.template.routes.AppRouter.{AppPage, baseUrl, routerConfig}
import scalajsreact.template.css.AppCSS

@JSExport
object ReactApp extends JSApp {

  @JSExport
  override def main(): Unit = {
    AppCSS.load
    dom.console.info("Router logging is enabled. Enjoy!")

    //val environmentVars : java.util.Map[String,String] = System.getenv()
    //for ((k,v) <- environmentVars) println(s"key: $k, value: $v")

   // val properties = System.getProperties()
    //properties.forEach(_ -> println())

   // properties.keySet().forEach(println)


    import java.util.Properties
    val props = System.getProperties
    val e = props.propertyNames

    while ( {
      e.hasMoreElements
    }) {
      val key = e.nextElement.asInstanceOf[String]
      System.out.println(key + " -- " + props.getProperty(key))
    }

    val router = Router(baseUrl, routerConfig.logToConsole)
    router() renderIntoDOM dom.document.body //.getElementById(s"reactRoot")
  }
}