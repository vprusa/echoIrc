package scalajsreact.template.models

import org.scalajs.dom._

/**
  * Created by vprusa on 4/6/17.
  */
case class IrcChatProps(var username: String, url: String, var ws: Option[WebSocket]) {

}
