package scalajsreact.template

import japgolly.scalajs.react.extra.router._
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalajsreact.template.models.IrcChatProps

//import scalacss.ScalatagsCss._
import scalajsreact.template.css.AppCSS
import scalajsreact.template.routes.AppRouter.{baseUrl, routerConfig}

//import chandu0101.scalajs.react.components.Implicits._


@JSExport
object ReactApp extends JSApp {

  @JSExport
  override def main(): Unit = {
    AppCSS.load
    dom.console.info("Router logging is enabled. Enjoy!")

    /*
    val environmentVars : java.util.Map[String,String] = System.getenv()
    for ((k,v) <- environmentVars) println(s"key: $k, value: $v")
    val properties = System.getProperties()
    properties.forEach(_ -> println())
    properties.keySet().forEach(println)
*/
    val props = System.getProperties
    val e = props.propertyNames

    while ( {
      e.hasMoreElements
    }) {
      val key = e.nextElement.asInstanceOf[String]
      System.out.println(key + " -- " + props.getProperty(key))
    }

    val router = Router(baseUrl, routerConfig.logToConsole)
    val rootNode = dom.document.createElement("div")
    rootNode.setAttribute("id", "reactAppRootNode")
    dom.document.body.appendChild(rootNode)
    router() renderIntoDOM rootNode
  }
}