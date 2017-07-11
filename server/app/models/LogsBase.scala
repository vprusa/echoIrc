package models

import java.io.{File, _}
import java.text.SimpleDateFormat
import java.util.Calendar

import play.api.Logger
import shared.SharedMessages._

class LogsBase(uniqueId: (String, String), LOG_FILENAME: String = s"ircLog-${new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(Calendar.getInstance().getTime())}.log") extends DataBase(uniqueId = uniqueId) {

  import java.text.SimpleDateFormat
  import java.util.Calendar

  val now = Calendar.getInstance().getTime()
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss")
  var currentTime = dateFormat.format(now)

  val DATA_FORMAT_DIR_NAME: String = s"default"

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


  def searchLogs(jsMsgRequest: JsMessageSearchLogsRequest): JsMessageSearchResults = {
    Logger.debug("searchLogs")
    import scala.io.Source
    import scala.util.matching.Regex
    //val pattern = "([a-cA-C])".r
    val pattern = new Regex(jsMsgRequest.regex)
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
            var lineNmb = 1
            fileLinesIterator.foreach(line => {
              Logger.debug(line.toString)
              pattern.findAllIn(line).foreach(foundString => {
                // add to result with line
                Logger.debug("readJsMsg")
                val readJsMsg = upickle.default.read[JsMessageBase](line)
                Logger.debug(readJsMsg.toString)
                if (readJsMsg.isInstanceOf[JsMessage]) {
                  val logSnippet = LogSnippet(line = lineNmb.toString, filename = logFile.getName, target = dir.getName, found = foundString, jsmsg = readJsMsg.asInstanceOf[JsMessage])
                  Logger.debug("logSnippet")
                  Logger.debug(logSnippet.toString)
                  snippets :+= logSnippet
                }
                Logger.debug(snippets.toString)
              })
              lineNmb = lineNmb + 1
            })
          })
        }
      })
      //targetDirDefault
    })
    val result = JsMessageSearchResults(results = snippets)
    Logger.debug("result.toString")
    Logger.debug(result.toString)
    result
  }
}
