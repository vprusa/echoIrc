package models

import java.io.{File, _}
import java.text.SimpleDateFormat
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Calendar
import com.typesafe.config.{ConfigList, ConfigObject}
import play.api.Logger
import shared.SharedMessages._
import scala.util.matching.Regex

import scala.io.Source

/**
  * Created by vprusa on 4/21/17.
  */

class LogWrapper(uniqueId: (String, String), targetName: String, LOG_FILENAME: String = s"ircLog-${new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(Calendar.getInstance().getTime())}.log")
  extends LogsBase(uniqueId, LOG_FILENAME) {

  val USER_DATA_TARGET_DIR: String = s"${USER_DATA_DIR}/targets/${targetName}/"
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

  def logLineAndExecuteScriptAction(line: JsMessageBase): Unit = {

    createDirIfNotExists()
    createLogFileIfNotExists()

    Logger.debug(s"LogLine: ${line}")

    val fw = new FileWriter(USER_LOG_JS_FILEPATH, true)
    try {
      Logger.debug(s"Added line: '${line}' to ${USER_LOG_JS_FILEPATH}")

      // execute script if regex matches something
      if (line.isInstanceOf[JsMessage]) {
        line.asInstanceOf[JsMessage].timeReceived = s"${new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(Calendar.getInstance().getTime())}"
      }
      val lineStr = upickle.default.write(line)

      val scriptBase: ScriptBase = ScriptBase.getScriptBase(uniqueId)

      if (!scriptBase.scriptsConfig.isEmpty || scriptBase.scriptsConfig.hasPath("mappers")) {

        val mappers = scriptBase.scriptsConfig.getObject("mappers")
        Logger.debug("mappers.toString")
        Logger.debug(mappers.toString)
        /*
            mappers.forEach({
                case (k, v) => {
                  Logger.debug("k,v")
                }
                case (s) => {
                  Logger.debug("k")
                }
              })
          */
        /*
        case (k: String, v: ConfigObject) => {
          Logger.debug("k")
          Logger.debug(k)
          Logger.debug("v")
          Logger.debug(v.toString)
          val filename = v.toConfig.getString("filename")
          val regex = v.toConfig.getString("regex")
          // new ScriptWrapper()

          val pattern = new Regex(regex)
          Logger.debug("Regex")
          Logger.debug(pattern.toString())

          pattern.findAllIn(lineStr).foreach(foundString => {
            // add to result with line
            Logger.debug(s"Executing script ${filename} with regex ${regex} for string: ${foundString}")
          })
        }
        */

      }

      fw.write(lineStr + '\n')
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
    Logger.debug("getLogsFiles:")
    Logger.debug(dir)
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def createSimpleIfNotExist(): Unit = {
    createDirIfNotExists(USER_LOG_SIMPLE_DIR)
  }

  def getSimpleLogsFiles(): List[File] = {
    getLogsFiles(USER_LOG_SIMPLE_DIR)
  }


}
