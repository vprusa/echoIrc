/**
  * Copyright 2012 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.i18n.{I18nSupport, MessagesApi}
import securesocial.core._
import service.{DemoUser, MyEnvironment, MyEventListener}
import play.api.mvc.{Action, RequestHeader}

import scala.concurrent.ExecutionContext

//class Application @Inject() (override implicit val env: MyEnvironment)
class Application @Inject()
(
  override implicit val env: MyEnvironment,
  implicit val webJarAssets: WebJarAssets,
  implicit val messagesApi: MessagesApi
)
/*(
                   override implicit val env: MyEnvironment,
                   implicit val actorSystem: ActorSystem,
                   implicit val webJarAssets: WebJarAssets,
                   implicit val mat: Materializer,
                   override implicit val executionContext: ExecutionContext,
                   val messagesApi: MessagesApi
                 )*/
  extends securesocial.core.SecureSocial with I18nSupport {

  /*@Inject
  override implicit val env: MyEnvironment
  @Inject
  implicit val actorSystem: ActorSystem
  @Inject
  implicit val webJarAssets: WebJarAssets
  @Inject
  implicit val mat: Materializer
  //   executionContext: ExecutionContext,
  @Inject
  implicit val messagesApi: MessagesApi
  */

  def index = SecuredAction {
    implicit request =>
      Ok(views.html.login.index(request.user.main))
  }

  // a sample action using an authorization implementation
  def onlyTwitter = SecuredAction(WithProvider("twitter")) {
    implicit request =>
      Ok("You can see this because you logged in using Twitter")
  }

  def linkResult = SecuredAction {
    implicit request =>
      Ok(views.html.login.linkResult(request.user))
  }

  /**
    * Sample use of SecureSocial.currentUser. Access the /current-user to test it
    */
  def currentUser = Action.async {
    implicit request =>
      SecureSocial.currentUser.map {
        maybeUser =>
          val userId = maybeUser.map(_.main.userId).getOrElse("unknown")
          Ok(s"Your id is $userId")
      }
  }
}

// An Authorization implementation that only authorizes uses that logged in using twitter
case class WithProvider(provider: String) extends Authorization[DemoUser] {
  def isAuthorized(user: DemoUser, request: RequestHeader) = {
    user.main.providerId == provider
  }
}
