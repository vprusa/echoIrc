package models

import play.api.Logger

import java.io.File
import scala.io.Source

import java.io._

import java.nio.file.{Paths, Files}

/**
  * Created by vprusa on 4/21/17.
  */

class Logs(userId: String) {

  def getCurrentDirectory = new java.io.File(".").getCanonicalPath

  val LOG_FILENAME: String = "ircLog.log"
  val LOG_FILEPATH: String = s"./ircLogs/${userId}/${LOG_FILENAME}"
  val LOG_DIR: String = s"${getCurrentDirectory}/ircLogs/${userId}/"

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

}
