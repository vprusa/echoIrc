package dao

import java.sql.Timestamp
import javax.inject.Inject

import org.joda.time.{DateTime, DateTimeZone}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import securesocial.core.providers.MailToken
import javax.inject.Inject

import securesocial.core.PasswordInfo
import slick.dbio.DBIOAction

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

import java.util.Date
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import org.joda.time._

import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.{GetResult, H2Profile, JdbcProfile}
import slick.jdbc.ActionBasedSQLInterpolation._
import java.util.{Locale, TimeZone}

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

/**
  * Created by vprusa on 4/28/17.
  */
class TokenDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api.{_}

  private val Tokens = TableQuery[MailTokens]

  def all() /* : Future[Seq[UserDAO]] */ = db.run(Tokens.result)

  def insert(token: MailToken): Future[Unit] = db.run(Tokens += token).map { _ => () }

  def findById(tokenId: String): Future[Option[MailToken]] = {
    db.run(Tokens.filter(r => r.uuid === tokenId).result.headOption) //.map { _ => () }
  }

  def save(token: MailToken): Future[MailToken] = {
    //db.run(Tokens += token).map { _ => () }
    //Future(token)
    Tokens.shaped.value.save(token)
  }

  def delete(uuid: String) = {
    // db.run(Tokens.filter(r => r.uuid === uuid).delete).map { _ => () }
    Tokens.shaped.value.delete(uuid)
  }

  def deleteExpiredTokens() = {
    /*val query = for {
      u <- Tokens if u.expirationTime < currentDate
    } yield u
    */
    //val q1 = Tokens.filter(_.expirationTime < currentDate)
    //val q1 = Tokens.filter(_.expirationTime < new LocalDate(2012, 12, 5))
    //val q1 = Tokens.filter(_.expirationTime < new LocalDate(2012, 12, 5))
    //db.run(q1.result).map { _ => () }
    //db.run(Tokens.filter(_.expirationTime isBefore < currentDate ).delete).map { _ => () }
    //db.run(query.result).map{ _ => () }
    //db.run(sql"DELETE ").map{ _ => () }
    // db.run(sql"delete from tokens where expirationTime < #${currentDate}".result)
    val currentDate: DateTime = new DateTime
    Tokens.shaped.value.deleteExpiredTokens(currentDate)
  }

  class MailTokens(tag: Tag) extends Table[MailToken](tag, "token") {
    /**
      * Mapping for using Joda Time.
      */
    implicit def dateTimeMapping = MappedColumnType.base[DateTime, java.sql.Timestamp](
      dt => new Timestamp(dt.getMillis),
      ts => new DateTime(ts.getTime, DateTimeZone.UTC))

    def uuid = column[String]("uuid")

    def email = column[String]("email")

    def creationTime = column[DateTime]("creationTime")

    def expirationTime = column[DateTime]("expirationTime")

    def isSignUp = column[Boolean]("isSignUp")

    def * = {
      val shapedValue = (uuid, email, creationTime, expirationTime, isSignUp) //.shaped
      shapedValue.<>({
        tuple =>
          MailToken(uuid = tuple._1,
            email = tuple._2,
            creationTime = tuple._3,
            expirationTime = tuple._4,
            isSignUp = tuple._5)
      }, {
        (t: MailToken) =>
          Some {
            (t.uuid,
              t.email,
              t.creationTime,
              t.expirationTime,
              t.isSignUp)
          }
      })
    }


    def findById(tokenId: String): Future[Option[MailToken]] = {
      //this.filter(x => x.email === email && x.providerId === providerId).firstOption
      //implicit session =>
      /*
      val q: Query[MailTokens, MailToken, Seq] = for {
        token <- Tokens if token.uuid === tokenId
      } yield token

      q.firstOption
      */

      db.run(Tokens.filter(r => r.uuid === tokenId).result.headOption) //.map { _ => () }
    }

    //    def save(token: MailToken): MailToken = withSession { implicit session =>
    def save(token: MailToken): Future[MailToken] = {
      // db.run(Tokens.filter(r => r.uuid === token.uuid).result.headOption)
      findById(token.uuid).map(x => {
        x match {
          case None =>
            db.run(Tokens += token).map { _ => () }
            token

          case Some(existingToken) =>
            val tokenRow = for {
              t <- Tokens if t.uuid === existingToken.uuid
            } yield t

            val updatedToken = token.copy(uuid = existingToken.uuid)
            tokenRow.update(updatedToken)
            updatedToken
        }
      })
      // db.run(Tokens += token).map { _ => () }
    }

    // def delete(uuid: String) = withSession { implicit session =>
    def delete(uuid: String) = {
      /* val q = for {
          t <- this if t.uuid === uuid
        } yield t
        q.delete*/
      db.run(Tokens.filter(r => r.uuid === uuid).delete).map { _ => () }
    }

    //    def deleteExpiredTokens(currentDate: DateTime) = withSession { implicit session =>
    def deleteExpiredTokens(currentDate: DateTime) = {
      /*      val q = for {
              t <- this if (t.expirationTime < currentDate)
            } yield t
            q.delete*/
      val q = for {
        v <- Tokens
      } yield v
      //db.run(Tokens.filter(r => r.expirationTime.<(new Rep(currentDate))).delete).map { _ => () }
      //db.run(Tokens.withFilter(create_*.)lter(_.expirationTime.<(currentDate)).delete).map { _ => () }
      //      db.run(q.list).map { (x:MailToken) => {
      //      x} }
      db.run(q.result.headOption).map {
        _ match {
          case Some(mailToken) => {
            if (mailToken.expirationTime.isBefore(currentDate)) {

              val q2 = for {
                v2 <- Tokens
                if v2.uuid === mailToken.uuid
              } yield v2

              // TODO Logger.debug("mailToken.expirationTime.isBefore(currentDate)")
              db.run(q2.result.headOption)
            }
          }
        }
        //}
      }
    }


  }

}
