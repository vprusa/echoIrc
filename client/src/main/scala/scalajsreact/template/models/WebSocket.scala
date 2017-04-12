package scalajsreact.template.models

import org.scalajs.dom.{MessageEvent, WebSocket}
import org.scalajs.dom.raw.{EventTarget, MessageEvent}

import scala.scalajs.js
import scala.collection.mutable.ListBuffer

/*
  * Created by vprusa on 4/11/17.
  */
//class WebSocket(var url: String = js.native, var protocol: String = js.native)
class ChatWebSocket(url: String) {

  val websocket: WebSocket= new WebSocket(url)

  var onmessages: ListBuffer[js.Function1[MessageEvent, _]] = ListBuffer.empty[js.Function1[MessageEvent, _]]

  //def onmessage(e: MessageEvent): Unit


}
