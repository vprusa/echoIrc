package scalajsreact.template.models

import org.scalajs.dom

/**
  * Created by vprusa on 4/10/17.
  */
object AppConfig {

  def loadFromPage(): Unit = {
    val element = dom.document.getElementById("reactData")
    var currentUserData = element.getAttribute("currentUser")

    dom.console.info("element -- " + element.toString)
    System.out.println("element -- " + element.toString)
  }


  val ircChatPropsTest = IrcChatProps(username = "UserBot", url = "ws://localhost:9000/chat", None)

}
