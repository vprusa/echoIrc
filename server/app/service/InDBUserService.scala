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

import scala.concurrent.Future
import securesocial.core.services.{SaveMode, UserService}
import dao.{TokenDAO /*, DBUser*/ , UserDAO}
import javax.inject.Inject

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
  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    userDao.find(providerId, userId)
  }

  /**
    * Finds a profile by email and provider
    *
    * @param email      - the user email
    * @param providerId - the provider id
    * @return an optional profile
    */
  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    userDao.findByEmailAndProvider(email, providerId)
  }


  private def findProfile(p: BasicProfile): /*Future[((String, String), DemoUser)] */ Future[Option[GenericProfile]] = {
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