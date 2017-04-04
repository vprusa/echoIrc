package controllers

import javax.inject.Singleton

import org.pircbotx.Configuration
import org.pircbotx.hooks.types.GenericMessageEvent
import play.api.Logger
import play.api.libs.json.Json

import scala.util.{Failure, Success}

//

import java.net.URL
import javax.inject.Inject

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import utils.{IrcListener, IrcLogBot, Protocol}

import scala.concurrent.{ExecutionContext, Future}


// http://stackoverflow.com/questions/37371698/could-not-find-implicit-value-for-parameter-messages-play-api-i18n-messages-in

/**
  * A very simple chat client using websockets.
  */
@Singleton
class ReactJsController @Inject()(implicit actorSystem: ActorSystem, webJarAssets: WebJarAssets,
                                  mat: Materializer,
                                  executionContext: ExecutionContext, override val messagesApi: MessagesApi)
  extends IrcWebController with I18nSupport {

  def react(any: String) = Action {
    Logger.info(this.getClass.getName)
    Logger.info("Path:" + any)
    Ok(views.html.reactJsExample("x"))
    //Ok(views.html.index(s"asd"))
  }

}
