package models

import java.io.File

import play.api.Logger
import java.io.File
import com.typesafe.config.{Config, ConfigFactory}

object ScriptBase {
  def getScriptBase(uniqueId: (String, String)): ScriptBase = {
    new ScriptBase(uniqueId: (String, String))
  }
}

/**
  * Created by vprusa on 6/23/17.
  */
class ScriptBase(uniqueId: (String, String)) extends DataBase(uniqueId = uniqueId) {

  val DATA_SCRIPTS_DIR_NAME: String = s"scripts"
  val DATA_SCRIPTS_DIR: String = s"${getCurrentDirectory}/${DATA_DIR}/${uniqueId._1}_${uniqueId._2}/${DATA_SCRIPTS_DIR_NAME}"

  val scriptsConfig: Config = loadConfig()

  def getUsersScriptFiles(): List[File] = {
    getFilesInDir(DATA_SCRIPTS_DIR)
  }

  def getUsersScriptFilesNames(): List[String] = {
    getUsersScriptFiles().map(_.getName)
  }

  def loadConfig(): Config = {
    // https://stackoverflow.com/questions/28966791/read-values-from-config-in-scala

    val config: Config = ConfigFactory.parseFile(new File(DATA_SCRIPTS_DIR + "/scripts.conf"))
    /*
    mappers {
      default {
        file = "default.sh"
        regex = ".*"
      }
    }
    */
    config
  }

  def loadScripts(): List[ScriptWrapper] = {
    List.empty[ScriptWrapper]
  }


}
