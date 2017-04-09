package shared

object SharedMessages {
  def itWorks = "It works!"

  object JsMessageTypes extends Enumeration {
    val Message,JoinChannel,LeaveChannel= Value
  }

  sealed trait JsMessageBase
  case class JsMessage(sender: String, target: String, msg: String) extends JsMessageBase
  case class JsMessageCmd(sender: String, target: String) extends JsMessageBase // remove example cmd
  case class JsMessageJoinChannel(sender: String, target: String) extends JsMessageBase
  case class JsMessageLeaveChannel(sender: String, target: String) extends JsMessageBase
  //case class JsMessageJoinChannel(override val sender: String, override val target: String) extends JsMessageCmd(sender,target)

  /*

  object JsMessageTypes extends Enumeration {
    val Message,JoinChannel,LeaveChannel= Value
  }

  sealed class JsMessageBase(msgType: JsMessageTypes.Value)

  case class JsMessage(msgType: JsMessageTypes.Value, sender: String, target: String, msg: String) extends JsMessageBase(msgType)

  case class JsMessageCmd(msgType: JsMessageTypes.Value, sender: String, target: String) extends JsMessageBase(msgType)

  */

}
