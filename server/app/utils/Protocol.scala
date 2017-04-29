package utils

import upickle.default.{ReadWriter, macroRW}

/**
  * Created by vprusa on 3/27/17.
  */
object Protocol {
  /*sealed trait Message

  implicit val readWriter: ReadWriter[Message] =
    macroRW[ChatMessage] merge macroRW[MemberJoined] merge macroRW[MemberLeft]

  case class ChatMessage(sender: String, message: String) extends Message
  case class MemberJoined(member: String, allMembers: Seq[String]) extends Message
  case class MemberLeft(member: String, allMembers: Seq[String]) extends Message
*/
}
