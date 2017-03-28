package models

import java.util.{ Date }
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import scala.language.postfixOps
import play.api.Logger

case class User(id: Long, name: String, address: String, designation: String)

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object User {

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val user = {
    get[Long]("user.id") ~
      get[String]("user.name") ~
      get[String]("user.address") ~
      get[String]("user.designation") map {
        case id ~ name ~ address ~ designation => User(id, name, address, designation)
      }
  }

  // -- Queries
  User
  /**
   * Retrieve a user from the id.
   */
  def findById(id: Long): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user where id = {id}").on('id -> id).as(user.singleOpt)
    }
  }

  /**
   * Return a page of (User).
   *
   * @param page Page to display
   * @param pageSize Number of users per page
   * @param orderBy User property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[User] = {

    val offest = pageSize * page

    DB.withConnection { implicit connection =>

      val users = SQL(
        """
          select * from user 
          where user.name like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """).on(
          'pageSize -> pageSize,
          'offset -> offest,
          'filter -> filter,
          'orderBy -> orderBy).as(user *)

      val totalRows = SQL(
        """
          select count(*) from user 
          where user.name like {filter}
        """).on(
          'filter -> filter).as(scalar[Long].single)

      Page(users, page, offest, totalRows)

    }

  }

  /**
   * Retrieve all user.
   *
   * @return
   */
  def findAll(): List[User] = {
    DB.withConnection { implicit connection =>
      try {
        SQL("select * from user order by name").as(user *)
      } catch {
        case ex: Exception => Logger.info("ERROR", ex); Nil
      }
    }
  }

  /**
   * Update a user.
   *
   * @param id The user id
   * @param user The user values.
   */
  def update(id: Long, user: User): Int = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update user
          set name = {name}, address = {address}, designation = {designation}
          where id = {id}
        """).on(
          'id -> id,
          'name -> user.name,
          'address -> user.address,
          'designation -> user.designation).executeUpdate()
    }
  }

  /**
   * Insert a new user.
   *
   * @param user The user values.
   */
  def insert(user: User): Option[Long] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into user values (
    		{id}, {name}, {address}, {designation}
          )
        """).on(
          'id -> Option.empty[Long],
          'name -> user.name,
          'address -> user.address,
          'designation -> user.designation).executeInsert()
    }
  }

  /**
   * Delete a user.
   *
   * @param id Id of the user to delete.
   */
  def delete(id: Long): Int = {
    DB.withConnection { implicit connection =>
      SQL("delete from user where id = {id}").on('id -> id).executeUpdate()
    }
  }

}
