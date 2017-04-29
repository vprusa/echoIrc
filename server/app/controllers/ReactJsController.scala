package controllers

import javax.inject.Singleton

import play.api.Logger
import service.MyEnvironment
import shared.Shared

//

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.ExecutionContext

// http://stackoverflow.com/questions/37371698/could-not-find-implicit-value-for-parameter-messages-play-api-i18n-messages-in

/**
  * A very simple chat client using websockets.
  */
@Singleton
class ReactJsController @Inject()(implicit actorSystem: ActorSystem,
                                  override implicit val env: MyEnvironment,
                                  override implicit val webJarAssets: WebJarAssets,
                                  override implicit val messagesApi: MessagesApi
                                 )

  extends IrcWebController()(actorSystem, env, webJarAssets, messagesApi) with I18nSupport {

  def securedTest = SecuredAction {
    implicit request =>
      Ok(views.html.login.linkResult(request.user))
  }

  def reactNotSecured(any: String) = Action {
    implicit request =>

      Logger.debug(this.getClass.getName)
      Logger.debug(request.toString())
      Logger.debug("Path: " + any)

      Ok(views.html.reactJsExample(null, actorSystem.settings.config.getString("app.websocket.url")))
  }


  def react(any: String) = SecuredAction {
    implicit request =>

      Logger.info(this.getClass.getName)
      Logger.info(request.toString())
      Logger.info("Path:" + any)

      Ok(views.html.reactJsExample(request.user, actorSystem.settings.config.getString("app.websocket.url")))
  }

}
