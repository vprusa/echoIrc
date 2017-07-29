package shared

object SharedMessages {

  sealed trait JsMessageBase

  case class TargetParticipant(name: String)

  case class JsMessageTestRequest(sender: String, target: String, msg: String) extends JsMessageBase
  case class JsMessageTestResponse(sender: String, target: String, msg: String) extends JsMessageBase

  case class JsMessage(sender: String, target: String, msg: String, var timeSend: String = "", var timeReceived: String = "") extends JsMessageBase

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

  // TODO
  //case class JsMessageStarBot(botName: String, autoJoinChannels: Array[String]) extends JsMessageBase

  case class JsMessageRequestTargetsParticipants(botName: String, targets: Array[String]) extends JsMessageBase

  case class JsMessageResponseTargetsParticipants(targetsParticipants: Map[String, Array[TargetParticipant]]) extends JsMessageBase

  // REST
  case class JsMessageSearchLogsRequest(regex: String, target: String) extends JsMessageBase

  case class JsMessageGetLogsNamesRequest() extends JsMessageBase

  case class JsMessageGetLogsNamesResponse(targetsWithFiles: Map[String, Array[String]]) extends JsMessageBase

  case class JsMessageGetLogRequest(target: String, filename: String)

  sealed trait LogDataBase

  case class LogSnippet(line: String, target: String, filename: String /*, target: String*/ , found: String, jsmsg: JsMessage)

  case class JsMessageSearchResults(results: Array[LogSnippet]) extends JsMessageBase

  //case class LogsStatsState(wordCount: java.lang.Integer) extends JsMessageBase

}
