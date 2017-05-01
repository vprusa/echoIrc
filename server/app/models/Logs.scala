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
  val LOG_USERDIR: String = s"./ircLogs/${userId}/"
  val LOG_FILEPATH: String = s"${LOG_USERDIR}${LOG_FILENAME}"
  val LOG_DIR: String = s"${getCurrentDirectory}/ircLogs/${userId}/"

  def rotateNow(): Unit = {
    Logger.debug("rotateLogsNow.jsonBody.map .rotateNow")
    currentTime = dateFormat.format(now)
  }

  def createLogFileIfNotExists(): Unit = {
    val file = new File(LOG_FILEPATH)
    val successful = file.createNewFile
    if (successful) { // creating the directory succeeded
      Logger.debug("file was created successfully")
    }
    else { // creating the directory failed
      Logger.debug("failed trying to create the file")
    }
  }

  def createDirIfNotExists(): Unit = {
    val dir = new File(LOG_DIR)

    Logger.debug(s"getCurrentDirectory ${getCurrentDirectory}")
    Logger.debug(s"LOG_DIR ${LOG_DIR}")

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

    val fw = new FileWriter(LOG_FILEPATH, true)
    try {
      Logger.debug(s"Added line: '${line}' to ${LOG_FILEPATH}")
      fw.write(line + '\n')
    }
    finally fw.close()
  }

  def loadLogLines(linesCount: Int, startAt: Int): List[String] = {
    val arr: List[String] = List.empty[String]
    Logger.debug(s"Loading logs file: ${LOG_FILEPATH}, lines count: ${linesCount}, starting at line: ${startAt}")
    for (line <- Source.fromFile(LOG_FILEPATH).getLines()) {
      Logger.debug(line)
      arr :+ line
    }
    return arr
  }

  def getLogsFiles(): List[File] = {
    val d = new File(LOG_DIR)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def getLogFile(f: File): Unit = {

  }

}
