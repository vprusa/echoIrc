package service

/**
  * Copyright 2012-2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
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

import javax.inject.{Inject, Singleton}

import controllers.CustomRoutesService
import dao.{TokenDAO, UserDAO}
import play.api.Configuration
import play.api.i18n.MessagesApi
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers._

import scala.collection.immutable.ListMap

//import com.github.tototoshi.slick.H2JodaSupport._
import scala.language.implicitConversions

@Singleton
class MyEnvironment @Inject()(override val configuration: Configuration, override val messagesApi: MessagesApi, tokenDao: TokenDAO,
                              userDao: UserDAO) extends RuntimeEnvironment.Default {

  override type U = DemoUser
  override implicit val executionContext = play.api.libs.concurrent.Execution.defaultContext
  override lazy val routes = new CustomRoutesService(configuration)
  //  override lazy val userService: InMemoryUserService = new InMemoryUserService()
  override lazy val userService: InDBUserService = new InDBUserService(tokenDao, userDao)
  override lazy val eventListeners = List(new MyEventListener())

  override lazy val providers = ListMap(
    // oauth 2 client providers
    //include(new FacebookProvider(routes, cacheService, oauth2ClientFor(FacebookProvider.Facebook))),
    include(new GitHubProvider(routes, cacheService, oauth2ClientFor(GitHubProvider.GitHub)))
    //include(new GoogleProvider(routes, cacheService,oauth2ClientFor(GoogleProvider.Google))),

    // username password
    //,include(new MyUsernamePasswordProvider[DemoUser](userService, avatarService, viewTemplates, passwordHashers))
    ,include(new UsernamePasswordProvider[DemoUser](userService, avatarService, viewTemplates, passwordHashers))
  )

}