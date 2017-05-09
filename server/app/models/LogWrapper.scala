package models

import play.api.Logger
import java.io.File

import scala.io.Source
import java.io._
import java.nio.file.{Files, Paths}
import java.text.SimpleDateFormat
import java.util.Calendar

import shared.SharedMessages._

/**
  * Created by vprusa on 4/21/17.
  */

case class LogSnippetRequest(fromDateTime: String, toDateTime: String, fromLine: Int, linesCount: Int, logLines: Array[String])

/*
object LogSnippet {

  //var logSnippet : LogSnippet = LogSnippet

  def get() = {

  }

}
*/
class LogsBase(uniqueId: (String, String), LOG_FILENAME: String = s"ircLog-${new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(Calendar.getInstance().getTime())}.log") {

  import java.util.Calendar
  import java.text.SimpleDateFormat

  val now = Calendar.getInstance().getTime()
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss")
  var currentTime = dateFormat.format(now)

  def getCurrentDirectory = new java.io.File(".").getCanonicalPath

  //val LOG_FILENAME: String = s"ircLog-${currentTime}.log"
  val conf = play.api.Play.current.configuration

  val DATA_DIR: String = s"${conf.getString("app.server.dataDir").getOrElse("data")}"
  val DATA_FORMAT_DIR_NAME: String = s"default"
  val USER_DATA_DIR: String = s"${getCurrentDirectory}/${DATA_DIR}/${uniqueId._1}_${uniqueId._2}/"

  def getLogsTargetsFiles(formatDir: String = DATA_FORMAT_DIR_NAME): List[File] = {
    val d = new File(USER_DATA_DIR)
    Logger.debug("Dir: USER_DATA_DIR")
    Logger.debug(USER_DATA_DIR)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isDirectory).toList
    } else {
      List[File]()
    }
  }

  def getLogsAllTargetsFilesNames(): JsMessageGetLogsNamesResponse = {
    var data = Map.empty[String, Array[String]]

    val dirs: List[File] = getLogsTargetsFiles()
    dirs.foreach(dir => {
      var targetsFilesNames = Array.empty[String]
      val targetsFormatDirs = dir.listFiles.filter(_.isDirectory).toList
      targetsFormatDirs.foreach(formatDir => {
        // dont need any other
        if (formatDir.getName.matches(DATA_FORMAT_DIR_NAME)) {
          val targetsFiles = formatDir.listFiles.filter(_.isFile).toList

          // here im in USERDIR_TARGET and i need to go to formated dir / default basic
          targetsFiles.foreach(file => {
            targetsFilesNames :+= file.getName
          })
          data += (dir.getName -> targetsFilesNames)
        }
      })
    })

    JsMessageGetLogsNamesResponse(targetsWithFiles = data)
  }


  def searchLogs(jsmsg: JsMessageSearchLogsRequest): SearchResults = {
    Logger.debug("searchLogs")
    import scala.io.Source
    import scala.util.matching.Regex
    //val pattern = "([a-cA-C])".r
    val pattern = new Regex(jsmsg.regex)
    Logger.debug("Regex")
    Logger.debug(pattern.toString())
    var snippets = Array.empty[LogSnippet]

    val targetDirs: List[File] = getLogsTargetsFiles()
    targetDirs.foreach(dir => {
      Logger.debug(dir.toString)
      val targetFormatDirs = dir.listFiles().filter(_.isDirectory).toList
      Logger.debug("defaultFormatTargetFiles ")
      Logger.debug(targetFormatDirs.toString())
      targetFormatDirs.foreach(targetDirOfFormat => {
        if (targetDirOfFormat.getName.matches("default")) {
          //  data/user_provider/{target}/default/...
          val targetsFiles = targetDirOfFormat.listFiles().filter(_.isFile).toList
          targetsFiles.foreach(logFile => {
            Logger.debug(logFile.toString)
            val fileLinesIterator = Source.fromFile(logFile).getLines //.toList
            var lineNmb = 0
            fileLinesIterator.foreach(line => {
              Logger.debug(line.toString)
              pattern.findAllIn(line).foreach(foundString => {
                // add to result with line
                Logger.debug("readJsMsg")
                val readJsMsg = upickle.default.read[JsMessageBase](line)
                Logger.debug(readJsMsg.toString)
                /*if (readJsMsg.isInstanceOf[JsMessage]) {
                  val logSnippet = LogSnippet(line = lineNmb, filename = logFile.getName, target = dir.getName, msg = readJsMsg.asInstanceOf[JsMessage])
                  Logger.debug(logSnippet.toString)
                  snippets :+ logSnippet
                }*/
              })
              lineNmb = lineNmb + 1
            })
          })
        }
      })
      //targetDirDefault
    })
    val result = SearchResults(request = jsmsg, results = snippets)
    Logger.debug("result.toString")
    Logger.debug(result.toString)
    result
  }
}

class LogWrapper(uniqueId: (String, String), targetName: String, LOG_FILENAME: String = s"ircLog-${new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(Calendar.getInstance().getTime())}.log")
  extends LogsBase(uniqueId, LOG_FILENAME) {

  val USER_DATA_TARGET_DIR: String = s"${USER_DATA_DIR}/${targetName}/"
  val USER_LOG_JS_DIR: String = s"${USER_DATA_TARGET_DIR}${DATA_FORMAT_DIR_NAME}/"
  val USER_LOG_JS_FILEPATH: String = s"${USER_LOG_JS_DIR}${LOG_FILENAME}"
  val USER_LOG_SIMPLE_DIR: String = s"${USER_DATA_TARGET_DIR}/simple/"
  val USER_LOG_SIMPLE_FILEPATH: String = s"${USER_LOG_SIMPLE_DIR}${LOG_FILENAME}"

  def rotateNow(): Unit = {
    Logger.debug("rotateLogsNow.jsonBody.map.rotateNow")
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

  def logLine(line: JsMessageBase): Unit = {
    createDirIfNotExists()
    createLogFileIfNotExists()
    Logger.debug(s"LogLine: ${line}")

    val fw = new FileWriter(USER_LOG_JS_FILEPATH, true)
    try {
      Logger.debug(s"Added line: '${line}' to ${USER_LOG_JS_FILEPATH}")
      fw.write(upickle.default.write(line) + '\n')
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
