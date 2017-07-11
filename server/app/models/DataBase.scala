package models

import java.io.File

import play.api.Logger

/**
  * Created by vprusa on 6/18/17.
  */
class DataBase(uniqueId: (String, String)) {
  def getCurrentDirectory = new java.io.File(".").getCanonicalPath

  //val LOG_FILENAME: String = s"ircLog-${currentTime}.log"
  val conf = play.api.Play.current.configuration

  val DATA_DIR: String = s"${conf.getString("app.server.dataDir").getOrElse("data")}"
  val USER_DATA_DIR: String = s"${getCurrentDirectory}/${DATA_DIR}/${uniqueId._1}_${uniqueId._2}/"

  def getFilesInDir(dirPath:String): List[File] = {
    val d = new File(dirPath)
    Logger.debug("Dir: dirName")
    Logger.debug(dirPath)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isDirectory).toList
    } else {
      List[File]()
    }
  }

}
