package service

import javax.inject._
import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future

trait Say {
  def hello(): Unit
  def goodbye(): Unit
}

@Singleton
class SayImpl @Inject() (appLifecycle: ApplicationLifecycle) extends Say {  
    override def hello(): Unit = println("Hello!")
    override def goodbye(): Unit = println("Goodbye!")

    // You can do this, or just explicitly call `hello()` at the end
    def start(): Unit = hello()

    // When the application starts, register a stop hook with the
    // ApplicationLifecycle object. The code inside the stop hook will
    // be run when the application stops.
    appLifecycle.addStopHook { () =>
        goodbye()
        Future.successful(())
    }

    // Called when this singleton is constructed (could be replaced by `hello()`)
    start()
}
