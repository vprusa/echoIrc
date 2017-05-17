package controllers

import java.util.concurrent.TimeoutException
import javax.inject.Singleton

import play.api.libs.concurrent.Promise
import service.MyEnvironment

import scala.concurrent.duration._


//

import javax.inject.Inject

import play.api.i18n.MessagesApi

import scala.concurrent.Future


// http://stackoverflow.com/questions/37371698/could-not-find-implicit-value-for-parameter-messages-play-api-i18n-messages-in

/**
  * A very simple chat client using websockets.
  */
@Singleton
class BaseController @Inject()  (
                                override implicit val env: MyEnvironment,
                                override implicit val webJarAssets: WebJarAssets,
                                override implicit val messagesApi: MessagesApi
                              )
  extends Application()(env, webJarAssets, messagesApi) {

  implicit val timeout = 10.seconds

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
