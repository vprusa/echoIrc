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
package service

import play.api.Logger
import securesocial.core._
import securesocial.core.providers.{MailToken, UsernamePasswordProvider}

import scala.concurrent.{Await, Future}
import securesocial.core.services.{SaveMode, UserService}
import dao.{TokenDAO, UserDAO}

import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
  * A Sample In Memory user service in Scala
  *
  * IMPORTANT: This is just a sample and not suitable for a production environment since
  * it stores everything in memory.
  */
class InDBUserService(
                       tokenDao: TokenDAO,
                       userDao: UserDAO
                     ) extends UserService[DemoUser] {

  val logger = Logger("application.controllers.InMemoryUserService")


  // on construct load from db
  // var users = Map[(String, String), DemoUser]()

  /**
    * Finds a SocialUser that maches the specified id
    *
    * @param providerId the provider id
    * @param userId     the user id
    * @return an optional profile
    */
  //def find(id: IdentityId):Option[Identity] = {
  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    Logger.debug("InDBUserService find")
    Logger.debug(s"providerId: ${providerId} userId: ${userId}")
    val conf = play.api.Play.current.configuration
    if (userId.contains("@")) {
      // find for email and userid is redirected here as password, username...
      Logger.debug("userId.contains@")

      import scala.concurrent.duration._
      //val result = Await.result(maybeCurUser, Duration.Inf)

      //Await.result(ret, Duration.Inf)
      //val storedUsers = Await.result(ret, 1.second)
      //Logger.debug(storedUsers.toString)
      val storedUsers = Await.result(userDao.all(), 1.second)
      Logger.debug("storedUsers.toString")
      Logger.debug(storedUsers.toString)
      Logger.debug(storedUsers.isEmpty.toString)

      val findResWaited = Await.result(userDao.findByEmailAndProvider(userId, "database"), 5 seconds)
      Logger.debug("findResWaited.toString")
      Logger.debug(findResWaited.toString)
      val ret = userDao.findByEmailAndProvider(userId, "database")
      Logger.debug(userDao.toString)
      ret
    } else if (userId != null
      && userId.matches(conf.getString("app.server.users.owner.username").getOrElse(null))
      && providerId.matches(conf.getString("app.server.users.owner.password").getOrElse(null))) {

      val ownerProfile = BasicProfile(
        "ownerProvider",
        "owner",
        Some(""),
        Some(""),
        Some(""),
        Some("owner@localhost"),
        None,
        AuthenticationMethod.UserPassword,
        None,
        None,
        Some(PasswordInfo("hasher", conf.getString("app.server.users.owner.password").getOrElse(null), None))
      )
      Logger.debug("ownerProfile.toString")
      Logger.debug(ownerProfile.toString)

      Logger.debug("findResWaited.toString")


      Logger.debug("findResWaited.toStringAll")
      val findResWaited00 = Await.result(userDao.all, 5 seconds)
      Logger.debug("findResWaited00.toString")
      Logger.debug(findResWaited00.toString)

      val findRes = userDao.find(providerId, userId)
      val findResWaited = Await.result(findRes, 5 seconds)
      Logger.debug("findResWaited.toString")
      Logger.debug(findResWaited.toString)
      if (findResWaited.isEmpty) {
        val storedUser = Await.result(userDao.insert(ownerProfile), 5 seconds)
        Logger.debug("storedUser.toString")
        Logger.debug(storedUser.toString)
        val findRes2 = userDao.find(providerId, userId)
        val findResWaited2 = Await.result(findRes2, 5 seconds)
        Logger.debug("findResWaited.toString")
        Logger.debug(findResWaited2.toString)


        Logger.debug("findResWaited.toStringAll")
        val findResWaited0 = Await.result(userDao.findByUserId(userId), 5 seconds)
        Logger.debug("findResWaited0.toString")
        Logger.debug(findResWaited0.toString)

        findRes2
      } else {
        Logger.debug("userDao.find(providerId, userId)")
        userDao.find(providerId, userId)
      }
    } else {
      //userDao.findByUserId(providerId,userId)
      Logger.debug("userDao.findByUserId(userId) else")
      Logger.debug("findResWaited.toStringAll")
      val findResWaited0 = Await.result(userDao.findByUserId(userId), 5 seconds)
      Logger.debug("findResWaited0.toString")
      Logger.debug("userId")
      Logger.debug(userId)
      Logger.debug("findResWaited0.toString")
      Logger.debug(findResWaited0.toString)


      userDao.findByUserId(userId)
    }
  }

  /**
    * Finds a profile by email and provider
    *
    * @param email      - the user email
    * @param providerId - the provider id
    * @return an optional profile
    */
  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    Logger.debug("InDBUserService findByEmailAndProvider")
    userDao.findByEmailAndProvider(email, providerId)
  }


  private def findProfile(p: BasicProfile): /*Future[((String, String), DemoUser)] */ Future[Option[GenericProfile]] = {
    Logger.debug("InDBUserService findProfile")
    userDao.find(p.providerId, p.userId).asInstanceOf[Future[Option[GenericProfile]]]
  }

  /*
    private def updateProfile(user: BasicProfile, entry: ((String, String), DemoUser)): Future[DemoUser] = {
      val identities = entry._2.identities
      val updatedList = identities.patch(identities.indexWhere(i => i.providerId == user.providerId && i.userId == user.userId), Seq(user), 1)
      val updatedUser = entry._2.copy(identities = updatedList)
      users = users + (entry._1 -> updatedUser)
      //Future.successful(updatedUser)
      //updatedUser
      //if (user.isInstanceOf[ BasicProfile])
      userDao.save(user)
      // Future(entry._2)
      Future.successful(updatedUser)
    }
  */
  /**
    * Saves a profile.  This method gets called when a user logs in, registers or changes his password.
    * This is your chance to save the user information in your backing store.
    *
    * @param profile the user profile
    * @param mode    a mode that tells you why the save method was called
    */
  def save(profile: BasicProfile, mode: SaveMode): Future[DemoUser] = {
    Logger.debug("InDBUserService save")
    mode match {
      case SaveMode.SignUp =>
        // val newUser = DemoUser(profile, List(profile))
        // users = users + ((profile.providerId, profile.userId) -> newUser)
        userDao.save(profile)
        Future.successful(DemoUser(profile))
      case SaveMode.LoggedIn =>
        // first see if there is a user with this BasicProfile already.
        import scala.concurrent.ExecutionContext.Implicits.global
        find(profile.userId, profile.providerId).map {
          _ match {
            case Some(existingUser: BasicProfile) => {
              //updateProfile(profile, existingUser)
              userDao.save(existingUser)
              DemoUser(profile)
            }
            case None => {
              val newUser = DemoUser(profile)

              //if (profile.isInstanceOf[DBUser])
              userDao.save(profile)
              //users = users + ((user.providerId, user.userId) -> newUser)
              //              Future.successful(newUser)
              newUser
            }
          }
        }

      case SaveMode.PasswordChange => {
        userDao.save(profile)
        Future.successful(DemoUser(profile))
      }
      // findProfile(profile).map { entry => updateProfile(profile, entry) }.getOrElse(
      // this should not happen as the profile will be there
      // throw new Exception("missing profile)")
      //)
    }
  }

  /**
    * Links the current user to another profile
    *
    * @param current The current user instance
    * @param to      the profile that needs to be linked to
    */
  def link(current: DemoUser, to: BasicProfile): Future[DemoUser] = {
    Logger.debug("InDBUserService link")
    userDao.save(to)
    Future.successful(DemoUser(to))
  }

  /**
    * Returns an optional PasswordInfo instance for a given user
    *
    * @param user a user instance
    * @return returns an optional PasswordInfo
    */
  def passwordInfoFor(user: DemoUser): Future[Option[PasswordInfo]] = {
    Logger.debug("InDBUserService passwordInfoFor")
    Future.successful(user.main.passwordInfo)
  }

  /**
    * Updates the PasswordInfo for a given user
    *
    * @param user a user instance
    * @param info the password info
    * @return
    */
  def updatePasswordInfo(user: DemoUser, info: PasswordInfo): Future[Option[BasicProfile]] = {
    Logger.debug("InDBUserService updatePasswordInfo")
    val profile: BasicProfile = user.main
    val newProfile: BasicProfile = profile.copy(passwordInfo = Some(info))
    userDao.save(newProfile)
    Future.successful(Some(newProfile))
  }

  /**
    * Saves a mail token.  This is needed for users that
    * are creating an account in the system or trying to reset a password
    *
    * Note: If you do not plan to use the UsernamePassword provider just provide en empty
    * implementation
    *
    * @param token The token to save
    */
  def saveToken(token: MailToken): Future[MailToken] = {
    Logger.debug("InDBUserService saveToken")
    tokenDao.save(token)
    Future.successful(token)
  }

  /**
    * Finds a token
    *
    * Note: If you do not plan to use the UsernamePassword provider just provide en empty
    * implementation
    *
    * @param token the token id
    * @return
    */
  def findToken(token: String): Future[Option[MailToken]] = {
    Logger.debug("InDBUserService findToken")
    tokenDao.findById(token)
  }

  /**
    * Deletes a token
    *
    * Note: If you do not plan to use the UsernamePassword provider just provide en empty
    * implementation
    *
    * @param uuid the token id
    */
  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    Logger.debug("InDBUserService deleteToken")
    tokenDao.delete(uuid)
    Future.successful(None)
  }

  /**
    * Deletes all expired tokens
    *
    * Note: If you do not plan to use the UsernamePassword provider just provide en empty
    * implementation
    *
    */
  def deleteExpiredTokens() = {
    Logger.debug("InDBUserService deleteExpiredTokens")
    tokenDao.deleteExpiredTokens()
  }

  /*

  def find(providerId: String, userId: String): Future[Option[DBUser]] = {
    if (logger.isDebugEnabled) {
      logger.debug("users = %s".format(userDao))
    }

    //Future.successful(result.headOption)
    userDao.find(providerId, userId)
  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[GenericProfile]] = {
    if (logger.isDebugEnabled) {
      logger.debug("users = %s".format(userDao))
    }
    userDao.findByEmailAndProvider(email, providerId)
  }

  private def findProfile(p: GenericProfile): Future[((String, String), DemoUser)] = {
    userDao.find(p.providerId, p.userId).asInstanceOf[Future[Option[GenericProfile]]]
  }

  private def updateProfile(user: GenericProfile, entry: ((String, String), DemoUser)): Future[DemoUser] = {
    val identities = entry._2.identities
    val updatedList = identities.patch(identities.indexWhere(i => i.providerId == user.providerId && i.userId == user.userId), Seq(user), 1)
    val updatedUser = entry._2.copy(identities = updatedList)
    //users = users + (entry._1 -> updatedUser)
    //Future.successful(updatedUser)
    //updatedUser
    if (user.isInstanceOf[DBUser])
      userDao.save(user.asInstanceOf[DBUser])
    Future(updatedUser)
  }

  def save(user: GenericProfile, mode: SaveMode): Future[DemoUser] = {
    mode match {
      case SaveMode.SignUp =>
        val newUser = DemoUser(user, List(user))
        //users = users + ((user.providerId, user.userId) -> newUser)
        Future.successful(newUser)
      case SaveMode.LoggedIn =>
        // first see if there is a user with this BasicProfile already.
        findProfile(user).map(
          _ match {
            case Some(existingUser) =>
              updateProfile(user, existingUser)

            case None =>
              val newUser = DemoUser(user, List(user))

              if (user.isInstanceOf[DBUser])
                userDao.save(user.asInstanceOf[DBUser])
              //users = users + ((user.providerId, user.userId) -> newUser)
              Future.successful(newUser)
          }
        )

      case SaveMode.PasswordChange =>
        findProfile(user).map { entry => updateProfile(user, entry) }.getOrElse(
          // this should not happen as the profile will be there
          throw new Exception("missing profile)")
        )
    }
  }

  def link(current: DemoUser, to: BasicProfile): Future[DemoUser] = {
    if (current.identities.exists(i => i.providerId == to.providerId && i.userId == to.userId)) {
      Future.successful(current)
    } else {
      val added = to :: current.identities
      val updatedUser = current.copy(identities = added)
      users = users + ((current.main.providerId, current.main.userId) -> updatedUser)
      Future.successful(updatedUser)
    }
  }

  def saveToken(token: MailToken): Future[MailToken] = {
    Future.successful {
      tokens += (token.uuid -> token)
      token
    }
  }

  def findToken(token: String): Future[Option[MailToken]] = {
    Future.successful {
      tokens.get(token)
    }
  }

  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    Future.successful {
      tokens.get(uuid) match {
        case Some(token) =>
          tokens -= uuid
          Some(token)
        case None => None
      }
    }
  }

  //  def deleteTokens(): Future {
  //    tokens = Map()
  //  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }

  override def updatePasswordInfo(user: DemoUser, info: PasswordInfo): Future[Option[BasicProfile]] = {
    Future.successful {
      for (
        found <- users.values.find(_ == user);
        identityWithPasswordInfo <- found.identities.find(_.providerId == UsernamePasswordProvider.UsernamePassword)
      ) yield {
        val idx = found.identities.indexOf(identityWithPasswordInfo)
        val updated = identityWithPasswordInfo.copy(passwordInfo = Some(info))
        val updatedIdentities = found.identities.patch(idx, Seq(updated), 1)
        val updatedEntry = found.copy(identities = updatedIdentities)
        users = users + ((updatedEntry.main.providerId, updatedEntry.main.userId) -> updatedEntry)
        updated
      }
    }
  }

  override def passwordInfoFor(user: DemoUser): Future[Option[PasswordInfo]] = {
    Future.successful {
      for (
        found <- users.values.find(u => u.main.providerId == user.main.providerId && u.main.userId == user.main.userId);
        identityWithPasswordInfo <- found.identities.find(_.providerId == UsernamePasswordProvider.UsernamePassword)
      ) yield {
        identityWithPasswordInfo.passwordInfo.get
      }
    }
  }
*/
  /*
  //
  var users = Map[(String, String), DemoUser]()
  //private var identities = Map[String, BasicProfile]()
  private var tokens = Map[String, MailToken]()

  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    if (logger.isDebugEnabled) {
      logger.debug("users = %s".format(users))
    }
    val result = for (
      user <- users.values;
      basicProfile <- user.identities.find(su => su.providerId == providerId && su.userId == userId)
    ) yield {
      basicProfile
    }
    Future.successful(result.headOption)
  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    if (logger.isDebugEnabled) {
      logger.debug("users = %s".format(users))
    }
    val someEmail = Some(email)
    val result = for (
      user <- users.values;
      basicProfile <- user.identities.find(su => su.providerId == providerId && su.email == someEmail)
    ) yield {
      basicProfile
    }
    Future.successful(result.headOption)
  }

  private def findProfile(p: BasicProfile) = {
    users.find {
      case (key, value) if value.identities.exists(su => su.providerId == p.providerId && su.userId == p.userId) => true
      case _ => false
    }
  }

  private def updateProfile(user: BasicProfile, entry: ((String, String), DemoUser)): Future[DemoUser] = {
    val identities = entry._2.identities
    val updatedList = identities.patch(identities.indexWhere(i => i.providerId == user.providerId && i.userId == user.userId), Seq(user), 1)
    val updatedUser = entry._2.copy(identities = updatedList)
    users = users + (entry._1 -> updatedUser)
    Future.successful(updatedUser)
  }

  def save(user: BasicProfile, mode: SaveMode): Future[DemoUser] = {
    mode match {
      case SaveMode.SignUp =>
        val newUser = DemoUser(user, List(user))
        users = users + ((user.providerId, user.userId) -> newUser)
        Future.successful(newUser)
      case SaveMode.LoggedIn =>
        // first see if there is a user with this BasicProfile already.
        findProfile(user) match {
          case Some(existingUser) =>
            updateProfile(user, existingUser)

          case None =>
            val newUser = DemoUser(user, List(user))
            users = users + ((user.providerId, user.userId) -> newUser)
            Future.successful(newUser)
        }

      case SaveMode.PasswordChange =>
        findProfile(user).map { entry => updateProfile(user, entry) }.getOrElse(
          // this should not happen as the profile will be there
          throw new Exception("missing profile)")
        )
    }
  }

  def link(current: DemoUser, to: BasicProfile): Future[DemoUser] = {
    if (current.identities.exists(i => i.providerId == to.providerId && i.userId == to.userId)) {
      Future.successful(current)
    } else {
      val added = to :: current.identities
      val updatedUser = current.copy(identities = added)
      users = users + ((current.main.providerId, current.main.userId) -> updatedUser)
      Future.successful(updatedUser)
    }
  }

  def saveToken(token: MailToken): Future[MailToken] = {
    Future.successful {
      tokens += (token.uuid -> token)
      token
    }
  }

  def findToken(token: String): Future[Option[MailToken]] = {
    Future.successful {
      tokens.get(token)
    }
  }

  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    Future.successful {
      tokens.get(uuid) match {
        case Some(token) =>
          tokens -= uuid
          Some(token)
        case None => None
      }
    }
  }

  //  def deleteTokens(): Future {
  //    tokens = Map()
  //  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }

  override def updatePasswordInfo(user: DemoUser, info: PasswordInfo): Future[Option[BasicProfile]] = {
    Future.successful {
      for (
        found <- users.values.find(_ == user);
        identityWithPasswordInfo <- found.identities.find(_.providerId == UsernamePasswordProvider.UsernamePassword)
      ) yield {
        val idx = found.identities.indexOf(identityWithPasswordInfo)
        val updated = identityWithPasswordInfo.copy(passwordInfo = Some(info))
        val updatedIdentities = found.identities.patch(idx, Seq(updated), 1)
        val updatedEntry = found.copy(identities = updatedIdentities)
        users = users + ((updatedEntry.main.providerId, updatedEntry.main.userId) -> updatedEntry)
        updated
      }
    }
  }

  override def passwordInfoFor(user: DemoUser): Future[Option[PasswordInfo]] = {
    Future.successful {
      for (
        found <- users.values.find(u => u.main.providerId == user.main.providerId && u.main.userId == user.main.userId);
        identityWithPasswordInfo <- found.identities.find(_.providerId == UsernamePasswordProvider.UsernamePassword)
      ) yield {
        identityWithPasswordInfo.passwordInfo.get
      }
    }
  }
  */
}

// a simple User class that can have multiple identities
//case class DemoUser(main: BasicProfile, identities: List[BasicProfile])