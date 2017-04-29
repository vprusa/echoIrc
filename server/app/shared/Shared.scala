package shared

import utils.IrcLogBot

object Shared {

  // persistent value
  var ircLogBotMap: Map[String, IrcLogBot] = Map.empty[String, IrcLogBot]

  var adminIrcBot: IrcBotBackendProcess = null

  private var data: String = "empty"

  def setData(d: String): Unit = data = d

  def getData: String = data
}
