package models

import play.api.Logger
import java.io.File

import scala.io.Source
import java.io._
import java.nio.file.{Files, Paths}
import java.text.SimpleDateFormat
import java.util.Calendar

/**
  * Created by vprusa on 4/21/17.
  */

case class LogSnippetRequest(fromDateTime: String, toDateTime: String, fromLine: Int, linesCount: Int, logLines: Array[String])

object LogSnippet {

  //var logSnippet : LogSnippet = LogSnippet

  def get() = {

  }

}

class Logs(userId: String, LOG_FILENAME: String = s"ircLog-${new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(Calendar.getInstance().getTime())}.log") {

  import java.util.Calendar
  import java.text.SimpleDateFormat

  val now = Calendar.getInstance().getTime()
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss")
  var currentTime = dateFormat.format(now)

  def getCurrentDirectory = new java.io.File(".").getCanonicalPath

  //val LOG_FILENAME: String = s"ircLog-${currentTime}.log"
  val conf = play.api.Play.current.configuration

  val DATA_DIR: String = s"${conf.getString("app.server.dataDir").getOrElse("data")}"
  val USER_DATA_DIR: String = s"${getCurrentDirectory}/${DATA_DIR}/${userId}/"
  val USER_LOG_JS_DIR: String = s"${USER_DATA_DIR}default/"
  val USER_LOG_JS_FILEPATH: String = s"${USER_LOG_JS_DIR}${LOG_FILENAME}"
  val USER_LOG_SIMPLE_DIR: String = s"${USER_DATA_DIR}/simple/"
  val USER_LOG_SIMPLE_FILEPATH: String = s"${USER_LOG_SIMPLE_DIR}${LOG_FILENAME}"

  def rotateNow(): Unit = {
    Logger.debug("rotateLogsNow.jsonBody.map .rotateNow")
    currentTime = dateFormat.format(now)
  }

  def createLogFileIfNotExists(): Unit = {
    val file = new File(USER_LOG_JS_FILEPATH)
    val successful = file.createNewFile
    if (successful) { // creating the directory succeeded
      Logger.debug("file was created successfully")
    }
    else { // creating the directory failed
      Logger.debug("failed trying to create the file")
    }
  }

  def createDirIfNotExists(dirPath: String = USER_LOG_JS_DIR): Unit = {
    val dir = new File(dirPath)

    Logger.debug(s"getCurrentDirectory ${getCurrentDirectory}")
    Logger.debug(s"LOG_DIR ${dirPath}")
    dir.mkdirs
    if (!dir.exists) {
      val successful = dir.mkdir
      if (successful) { // creating the directory succeeded
        Logger.debug("directory was created successfully")
      }
      else { // creating the directory failed
        Logger.debug("failed trying to create the directory")
      }
    }
  }

  def logLine(line: String): Unit = {
    createDirIfNotExists()
    createLogFileIfNotExists()
    Logger.debug(s"LogLine: ${line}")

    val fw = new FileWriter(USER_LOG_JS_FILEPATH, true)
    try {
      Logger.debug(s"Added line: '${line}' to ${USER_LOG_JS_FILEPATH}")
      fw.write(line + '\n')
    }
    finally fw.close()
  }

  def loadLogLines(linesCount: Int, startAt: Int): List[String] = {
    val arr: List[String] = List.empty[String]
    Logger.debug(s"Loading logs file: ${USER_LOG_JS_FILEPATH}, lines count: ${linesCount}, starting at line: ${startAt}")
    for (line <- Source.fromFile(USER_LOG_JS_FILEPATH).getLines()) {
      Logger.debug(line)
      arr :+ line
    }
    return arr
  }

  def getLogsFiles(dir: String = USER_LOG_JS_DIR): List[File] = {
    val d = new File(dir)
    Logger.debug("Dir: USER_LOG_JS_DIR")
    Logger.debug(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  // parse JsMessages to Human-readable text (ignore all bot messages except JsMessage)
  def createSimpleIfNotExist(): Unit = {
    createDirIfNotExists(USER_LOG_SIMPLE_DIR)
  }

  def getSimpleLogsFiles(): List[File] = {
    getLogsFiles(USER_LOG_SIMPLE_DIR)
  }

}
