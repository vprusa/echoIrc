package utils

import akka.actor.{Actor, Props}
import com.typesafe.config.Config
import play.Logger
import play.api.Application
import shared.Shared
import java.text.SimpleDateFormat
import java.util.Calendar
/**
  * Created by vprusa on 5/10/17.
  */
class Planner(app: Application, cfg: Config) {

  // http://doc.akka.io/docs/akka/2.0/scala/scheduler.html

  val RotateLogs = "RotateLogs"
  val rotateLogsActor = app.actorSystem.actorOf(
    Props(
      new Actor {
        def receive = {
          case RotateLogs ⇒ {
            //Do something
            rotateLogs()
          }
        }
      }
    ))

  def rotateLogs(): Unit = {
    Shared.ircLogBotMap.foreach(logBot => {
      logBot._2.defaultListener.logHandlers.foreach(logHandler => {
        logHandler._2.rotateNow()
      })
    })
  }

  def planFutureLogRotation(): Unit = {
    // schedule method call for time
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    val configInterval = cfg.getInt("app.irc.defaultLogRotationInterval")
//    Logger.debug("configInterval")
//    Logger.debug(configInterval.toString)

    val configIntervalDelay = cfg.getInt("app.irc.defaultLogRotationMidnightMinutesDelay")

    // get minutes till midnight, todo custom time in day?
    val now = Calendar.getInstance()
    val currentMinute = now.get(Calendar.MINUTE)
    val currentHouse = now.get(Calendar.HOUR_OF_DAY)

    val minutesToMidnight = (24-currentHouse)*60 - currentMinute

    val rotationDelay = minutesToMidnight + configIntervalDelay

    //This will schedule to send the Tick-message
    //to the tickActor after 0ms repeating every 50ms
    val cancellable = app.actorSystem.scheduler.schedule(rotationDelay minutes,
      configInterval seconds,
      rotateLogsActor,
      RotateLogs)

  }

}
