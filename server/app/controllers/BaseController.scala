package controllers

import java.util.concurrent.TimeoutException
import javax.inject.Singleton

import models._
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Promise
import play.api.libs.json.Json

import scala.concurrent.duration._


//

import java.net.URL
import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import utils.{IrcListener, Protocol}

import scala.concurrent.{ExecutionContext, Future}


// http://stackoverflow.com/questions/37371698/could-not-find-implicit-value-for-parameter-messages-play-api-i18n-messages-in

/**
  * A very simple chat client using websockets.
  */
@Singleton
class BaseController @Inject()(implicit actorSystem: ActorSystem, webJarAssets: WebJarAssets,
                                mat: Materializer,
                                executionContext: ExecutionContext, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  implicit val timeout = 10.seconds

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def dashboard = Action {
    Ok(views.html.dashboard("Your new application is ready."))
  }

  object TimeoutFuture {

    def apply[A](block: => A)(implicit timeout: FiniteDuration): Future[A] = {

      val promise = scala.concurrent.Promise[A]()

      // if the promise doesn't have a value yet then this completes the future with a failure
      Promise.timeout(Nil, timeout).map(_ => promise.tryFailure(new TimeoutException("This operation timed out")))

      // this tries to complete the future with the value from block
      Future(promise.success(block))

      promise.future
    }

  }

}
