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
/*@Singleton
class ReactJsController @Inject()(implicit actorSystem: ActorSystem, webJarAssets: WebJarAssets,
                                  mat: Materializer,
                                  executionContext: ExecutionContext, override val messagesApi: MessagesApi)
  extends IrcWebController with I18nSupport {
*/

@Singleton
class ReactJsController @Inject()(implicit actorSystem: ActorSystem,
                                  override implicit val env: MyEnvironment,
                                  override implicit val webJarAssets: WebJarAssets,
                                  override implicit val messagesApi: MessagesApi
                                 )

//  extends Controller with I18nSupport {
//  extends Application(env)(actorSystem, webJarAssets, mat, messagesApi) {
//extends Application(env, actorSystem, webJarAssets, mat, executionContext, messagesApi) {
  extends IrcWebController()(actorSystem, env, webJarAssets, messagesApi) with I18nSupport {

  def securedTest = SecuredAction {
    implicit request =>
      Ok(views.html.login.linkResult(request.user))
  }


  def react(any: String) = SecuredAction {
    implicit request =>

      Logger.info(this.getClass.getName)
      Logger.info(request.toString())
      Logger.info("Path:" + any)
      //persistanObj += "2"
      //Logger.info(s"persistanObj: ${persistanObj}")
      Shared.setData(Shared.getData + "0")
      Logger.debug(s"Shared.getData() ${Shared.getData} ")

      Ok(views.html.reactJsExample(request.user,any))
  }

}
