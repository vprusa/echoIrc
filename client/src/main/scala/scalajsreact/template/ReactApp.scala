package scalajsreact.template

import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._
import vdom.html_<^._

import scalacss.ScalaCssReact._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router._
import org.scalajs.dom.document

//import scalacss.ScalatagsCss._
import pages._
import scalajsreact.template.routes.AppRouter.{AppPage, baseUrl, routerConfig}
import scalajsreact.template.css.AppCSS

//import chandu0101.scalajs.react.components.Implicits._

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
    //import css.MyStyles
    //MyStyles.addToDocument()

    val router = Router(baseUrl, routerConfig.logToConsole)
    val rootNode = dom.document.createElement("div")
    rootNode.setAttribute("id","reactAppRootNode")
    dom.document.body.appendChild(rootNode)
    router() renderIntoDOM rootNode //dom.document.body //getElementById(s"reactRoot")
  }
}