package controllers

import javax.inject.Singleton

import play.api.Logger
import service.MyEnvironment

import scala.collection.mutable.ListBuffer

//

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

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
      val topMenuList = actorSystem.settings.config.getStringList("app.client.adminPages")
      val channelsList = actorSystem.settings.config.getStringList("app.irc.defaultChannels")

      val asJson: String = s"""["topMenuList":${upickle.default.write(listToListBuffer(topMenuList))}, "channelsList":${upickle.default.write(listToListBuffer(channelsList))}]"""
      Logger.info(asJson)

      val data = utils.ReactViewData(null, actorSystem.settings.config.getString("app.websocket.url"), "/rest", topMenuList, channelsList, asJson)

      Ok(views.html.reactJs(data))
  }


  def react(any: String) = SecuredAction {
    implicit request =>

      val topMenuList = actorSystem.settings.config.getStringList("app.client.adminPages")
      val channelsList = actorSystem.settings.config.getStringList("app.irc.defaultChannels")

      val asJson: String = s"""{"topMenuList":${upickle.default.write(listToListBuffer(topMenuList))}, "channelsList":${upickle.default.write(listToListBuffer(channelsList))}}"""

      val data = utils.ReactViewData(request.user.main.userId, actorSystem.settings.config.getString("app.websocket.url"), "/rest", topMenuList, channelsList, asJson)

      Ok(views.html.reactJs(data))
  }

  def listToListBuffer(list: java.util.List[String]): ListBuffer[String] = {
    var topMenuItems = ListBuffer.empty[String]
    val i = list.iterator()
    while (i.hasNext) {
      val v: String = i.next()
      topMenuItems += v
    }
    topMenuItems
  }

}
