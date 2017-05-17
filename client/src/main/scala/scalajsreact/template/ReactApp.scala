package scalajsreact.template

import japgolly.scalajs.react.extra.router._
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

//import scalacss.ScalatagsCss._
import scalajsreact.template.css.AppCSS
import scalajsreact.template.routes.AppRouter.{baseUrl, loadMenuFromDom, routerConfig}

@JSExport
object ReactApp extends JSApp {

  @JSExport
  override def main(): Unit = {
    AppCSS.load
    dom.console.info("Router logging is enabled. Enjoy!")
    val element = dom.document.getElementById("reactData")

    dom.console.info("element -- " + element.toString)
    System.out.println("element -- " + element.toString)

    val router = Router(baseUrl, routerConfig.logToConsole)
    val rootNode = dom.document.createElement("div")
    rootNode.setAttribute("id", "reactAppRootNode")

    loadMenuFromDom()

    dom.document.body.appendChild(rootNode)
    router() renderIntoDOM rootNode
  }
}