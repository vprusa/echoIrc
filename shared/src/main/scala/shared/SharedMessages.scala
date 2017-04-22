package shared

object SharedMessages {
  def itWorks = "It works!"

  object JsMessageTypes extends Enumeration {
    val Message,JoinChannel,LeaveChannel= Value
  }

  sealed trait JsMessageBase
  case class JsMessage(sender: String, target: String, msg: String) extends JsMessageBase
  case class JsMessageOther(sender: String, target: String, msg: String) extends JsMessageBase
  case class JsMessageCmd(sender: String, target: String) extends JsMessageBase // remove example cmd
  case class JsMessageJoinChannel(sender: String, target: String) extends JsMessageBase
  case class JsMessageLeaveChannel(sender: String, target: String) extends JsMessageBase
  case class JsMessageIrcBotReady() extends JsMessageBase
  case class JsMessageStarBot(botName: String, autoJoinChannels: Array[String]) extends JsMessageBase

}
