package shared

import utils.IrcLogBot

object Shared {

  var ircLogBotMap: Map[(String, String), IrcLogBot] = Map.empty[(String, String), IrcLogBot]

  //var adminIrcBot: IrcBotBackendProcess = null

  private var data: String = "empty"

  def setData(d: String): Unit = data = d

  def getData: String = data
}
