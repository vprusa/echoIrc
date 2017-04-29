package dao

/*
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

import securesocial.core.PasswordInfo

//import models.User
//import models.Users
import org.joda.time.{DateTime}
import org.joda.time.DateTimeZone
import java.sql.Timestamp
import org.joda.time.LocalDateTime
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import securesocial.core.{AuthenticationMethod, OAuth1Info, OAuth2Info}
import slick.jdbc.JdbcProfile
import slick.jdbc.H2Profile.api._
import slick.util.TupleMethods._

//import slick.lifted._ //{ProvenShape, TableQuery, Tag}
import slick.lifted._ //{ProvenShape, TableQuery, Tag}

/**
  * Created by vprusa on 4/28/17.
  */
class TablesDAO {


  trait WithDefaultSession {
    def withSession[T](block: (Session => T)) = {
      val conf = play.api.Play.current.configuration //maybeApplication.map(_.configuration).getOrElse(play.api.Configuration.empty)
      val databaseURL = conf.getString("db.default.url").get
      val databaseDriver = conf.getString("db.default.driver").get
      val databaseUser = conf.getString("db.default.user").getOrElse("")
      val databasePassword = conf.getString("db.default.password").getOrElse("")

      val database = Database.forURL(url = databaseURL, driver = databaseDriver, user = databaseUser, password = databasePassword)

      //database withSession { session =>
      //  block(session)
      //}
      database.createSession()
    }
  }

  object Tables extends WithDefaultSession {
    val MailTokens = new TableQuery[MailTokens](new MailTokens(_)) {
      def findById(tokenId: String): Option[MailToken] = withSession {
        implicit session =>
          val q: Query[MailTokens, MailToken, Seq] = for {
            token <- this if token.uuid === tokenId
          } yield token
          q.firstOption
      }

      def save(token: MailToken): MailToken = withSession { implicit session =>
        findById(token.uuid) match {
          case None =>
            this.forUpdate insert (token)
            token

          case Some(existingToken) =>
            val tokenRow = for {
              t <- this if t.uuid === existingToken.uuid
            } yield t

            val updatedToken = token.copy(uuid = existingToken.uuid)
            tokenRow.update(updatedToken)
            updatedToken
        }
      }

      def delete(uuid: String) = withSession { implicit session =>
        val q = for {
          t <- this if t.uuid === uuid
        } yield t
        q.delete
      }

      def deleteExpiredTokens(currentDate: DateTime) = withSession { implicit session =>
        val q = for {
          t <- this if (t.expirationTime < currentDate)
        } yield t
        q.delete
      }
    }

    val Users = new TableQuery[Users](new Users(_)) {
      def autoInc = this.map(_.uid)

      def findById(id: Long) = withSession { implicit session =>
        val q = for {
          user <- this if user.uid === id
        } yield user
        q.firstOption
      }

      def findByEmailAndProvider(email: String, providerId: String): Option[User] = withSession { implicit session =>
        this.filter(x => x.email === email && x.providerId === providerId).firstOption
      }

      def findByIdentityId(userId: String, providerId: String = "userpass"): Option[User] = withSession { implicit session =>
        filter(x => x.userId === userId && x.providerId === providerId).firstOption
      }

      def all = withSession { implicit session => this.list
      }

      def save(user: User): User = withSession { implicit session =>
        findByIdentityId(user.identityId) match {
          case None =>
            val uid = autoInc.insert(user)
            user.copy(uid = Some(uid))

          case Some(existingUser) =>
            val userRow = for {
              u <- this if u.uid === existingUser.uid
            } yield u

            val updatedUser = user.copy(uid = existingUser.uid)
            userRow.update(updatedUser)
            updatedUser

        }
      }
    }
  }

}
*/
