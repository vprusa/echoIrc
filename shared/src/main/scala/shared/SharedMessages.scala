package shared

object SharedMessages {
  def itWorks = "It works!"

  object JsMessageTypes extends Enumeration {
    val Message, JoinChannel, LeaveChannel = Value
  }

  case class TargetParticipant(name: String)

  sealed trait JsMessageBase

  case class JsMessage(sender: String, target: String, msg: String) extends JsMessageBase

  case class JsMessageOther(sender: String, target: String, msg: String) extends JsMessageBase

  case class JsMessageCmd(sender: String, target: String) extends JsMessageBase // remove example cmd

  case class JsMessageJoinChannelRequest(sender: String, target: String) extends JsMessageBase

  case class JsMessageJoinChannelResponse(sender: String, target: String, participants: Array[TargetParticipant]) extends JsMessageBase

  case class JsMessageLeaveChannel(sender: String, target: String) extends JsMessageBase

  case class JsMessageLeaveChannelResponse(sender: String, target: String) extends JsMessageBase

  case class JsMessageRotateLogs(sender: String, target: String) extends JsMessageBase

  case class JsMessageTargetParticipantsRequest(sender: String, target: String) extends JsMessageBase

  case class JsMessageTargetParticipantsResponse(sender: String, target: String, participants: Array[TargetParticipant]) extends JsMessageBase

  case class JsMessageIrcBotReady() extends JsMessageBase

  case class JsMessageStarBot(botName: String, autoJoinChannels: Array[String]) extends JsMessageBase


}
