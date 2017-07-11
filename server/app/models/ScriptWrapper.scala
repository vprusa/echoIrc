package models

import java.io.File

import play.api.Logger

import sys.process._
import shared.SharedMessages.JsMessageGetLogsNamesResponse

/**
  * Created by vprusa on 6/18/17.
  */
class ScriptWrapper(uniqueId: (String, String), scriptName: String = "default.sh", scriptRegex: String = ".*") extends ScriptBase(uniqueId = uniqueId) {

  val DATA_SCRIPTS_FILE: String = s"${DATA_SCRIPTS_DIR}/${scriptName}"

  def loadScript(): Unit = {
    var data = Map.empty[String, Array[String]]
  }

  def saveScript(fileName: String, lines: Array[String]): Unit = {
    // todo or remove
  }

  def exec(): Int = {
    Logger.debug(s"executing: ${DATA_SCRIPTS_FILE}")
    val cmd = s"${DATA_SCRIPTS_FILE}"
    val exitCode = cmd.!
    exitCode
  }

}
