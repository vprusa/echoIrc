package dao

import javax.inject.Inject

import play.Logger
import securesocial.core.PasswordInfo
//import play.api.Logger
import securesocial.core.BasicProfile

import scala.concurrent.{Await, ExecutionContext, Future}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import securesocial.core.{AuthenticationMethod, OAuth1Info, OAuth2Info}
import slick.jdbc.JdbcProfile

//import slick.lifted._ //{ProvenShape, TableQuery, Tag} //{ProvenShape, TableQuery, Tag}

/*
case class User(
                 userId: String,
                 firstName: String,
                 lastName: String,
                 fullName: String,
                 email: Option[String],
                 avatarUrl: Option[String],
                 authMethod: AuthenticationMethod,
                 oAuth1Info: Option[OAuth1Info],
                 oAuth2Info: Option[OAuth2Info],
                 passwordInfo: Option[PasswordInfo] = None,
                 providerId: String = "userpass",
                 uid: Option[Long] = None
               )
*/

/*
trait GenericProfile extends UserProfile {
  def firstName: Option[String]
  def lastName: Option[String]
  def fullName: Option[String]
  def email: Option[String]
  def avatarUrl: Option[String]
  def authMethod: AuthenticationMethod
  def oAuth1Info: Option[OAuth1Info]
  def oAuth2Info: Option[OAuth2Info]
  def passwordInfo: Option[PasswordInfo]
}
*/

/*
case class BasicProfile(
                 userId: String,
                 firstName: String,
                 lastName: String,
                 fullName: String,
                 email: Option[String],
                 avatarUrl: Option[String],
                 authMethod: AuthenticationMethod,
                 oAuth1Info: Option[OAuth1Info],
                 oAuth2Info: Option[OAuth2Info],
                 passwordInfo: Option[PasswordInfo] = None,
                 providerId: String = "userpass",
                 uid: Option[Long] = None
               ) extends GenericProfile
*/

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val Users = TableQuery[Users]

  def all() /* : Future[Seq[UserDAO]] */ = db.run(Users.result)

  def insert(user: BasicProfile): Future[Unit] = db.run(Users += user).map { _ => () }

  def autoInc = Users.map(_.uid)

  def findById(id: Long) = {
    /*      val q = for {
            user <- this if user.uid === id
          } yield user
          q.firstOption*/

    db.run(Users.filter(_.uid === id).result).map { _ => () }
  }

  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    val q = for {
      user <- Users
      if user.userId === userId
      if user.providerId === providerId
    } yield user
    //db.run(Users.filter(_.userId === userId).filter(_.providerId === providerId).result.headOption).map { _ => () }
    db.run(q.result.headOption) //.map { _ => () }
  }

  def findByUserId(userId: String): Future[Option[BasicProfile]] = {
    db.run(Users.filter(_.email === userId).result.headOption)
  }

  // def findByEmailAndProvider(email: String, providerId: String): Option[User] = withSession { implicit session =>
  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    val ret = db.run(Users.filter(x => x.email === email && x.providerId === providerId).result.headOption)
    ret
  }

  // def findByIdentityId(userId: String, providerId: String = "userpass"): Option[User] = withSession { implicit session =>
  def findByIdentityId(userId: String, providerId: String = "userpass"): Future[Option[BasicProfile]] = {
    db.run(Users.filter(x => x.userId === userId && x.providerId === providerId).result.headOption)
  }

  // def all = withSession { implicit session => this.list
  //}

  //def save(user: User): User = withSession { implicit session =>
  def save(user: BasicProfile): BasicProfile = {
    /*findByIdentityId(user.identityId) match {
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

    }*/
    find(user.userId, user.providerId).map { fut =>
      fut match {
        case v: Option[BasicProfile] => {
          if (v.isEmpty) {
//            Logger.debug("save found None")
            // added new
            val res = db.run(Users += user).map { _ => () }

            import scala.concurrent.duration.DurationInt
            //            import scala.concurrent.ExecutionContext.Implicits.global
            import scala.concurrent.{Await, Future}

            val storedUsers = Await.result(db.run(Users.result), 1 seconds)
//            Logger.debug("DB test2")
//            Logger.debug(storedUsers.toString())
          } else {
            v
            v.map((b: BasicProfile) => {
              // update
              db.run(Users.filter(t => t.userId === b.userId && t.providerId === b.providerId).map(k => ()).update(user))
            })
          }
        }
        // todo remove, should not be necessary
        case None => {
//          Logger.debug("save found None")
          // added new
          db.run(Users += user).map { _ => () }
        }
      }
    }
    user
  }

  //val UsersTable = new TableQuery[Users](new Users(_)) {
  //}

  /*private class UsersTable(tag: Tag) extends Table[Users](tag, "USERS") {

    def name = column[String]("NAME", O.PrimaryKey)

    def color = column[String]("COLOR")

    def * = (name, color) <> (User.tupled, User.unapply)
  }*/

  class Users(tag: Tag) extends Table[BasicProfile](tag, "user") {

    import slick.driver.H2Driver

    implicit def string2AuthenticationMethod: H2Driver.BaseColumnType[AuthenticationMethod] =
      MappedColumnType.base[AuthenticationMethod, String](
        authenticationMethod => authenticationMethod.method,
        string => AuthenticationMethod(string)
      )

    implicit def tuple2OAuth1Info(tuple: (Option[String], Option[String])): Option[OAuth1Info] = tuple match {
      case (Some(token), Some(secret)) => Some(OAuth1Info(token, secret))
      case _ => None
    }

    implicit def tuple2OAuth2Info(tuple: (Option[String], Option[String], Option[Int], Option[String])): Option[OAuth2Info] = tuple match {
      case (Some(token), tokenType, expiresIn, refreshToken) => Some(OAuth2Info(token, tokenType, expiresIn, refreshToken))
      case _ => None
    }

    def uid = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[String]("userId")

    def providerId = column[String]("providerId")

    def email = column[Option[String]]("email")

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def fullName = column[String]("fullName")

    def authMethod = column[AuthenticationMethod]("authMethod")

    def avatarUrl = column[Option[String]]("avatarUrl")

    // oAuth 1
    def token = column[Option[String]]("token")

    def secret = column[Option[String]]("secret")

    // oAuth 2
    def accessToken = column[Option[String]]("accessToken")

    def tokenType = column[Option[String]]("tokenType")

    def expiresIn = column[Option[Int]]("expiresIn")

    def refreshToken = column[Option[String]]("refreshToken")

    //def passwordInfo = column[Option[PasswordInfo]]("passwordInfo")
    def passwordInfo = column[Option[String]]("passwordInfo")


    def * = {
      val shapedValue = (uid.?,
        userId,
        providerId,
        firstName,
        lastName,
        fullName,
        email,
        avatarUrl,
        authMethod,
        token,
        secret,
        accessToken,
        tokenType,
        expiresIn,
        refreshToken,
        passwordInfo
      ).shaped

//      Logger.debug("* converting shapedValue")
//      Logger.debug(shapedValue.toString)

      shapedValue.<>({
        tuple =>
//          Logger.debug("* converting shapedValue to tuples")
//          Logger.debug(tuple.toString)

          val bp = BasicProfile(
            //uid = tuple._1,
            userId = tuple._2,
            providerId = tuple._3,
            firstName = Some(tuple._4),
            lastName = Some(tuple._5),
            fullName = Some(tuple._6),
            email = tuple._7,
            avatarUrl = tuple._8,
            authMethod = tuple._9,
            oAuth1Info = (tuple._10, tuple._11),
            oAuth2Info = (tuple._12, tuple._13, tuple._14, tuple._15)
            , passwordInfo = Some(PasswordInfo(hasher = "bcrypt", password = tuple._16.getOrElse("default")))
          )
//          Logger.debug("* converting bp")
//          Logger.debug(bp.toString)

          bp
      }, { (u: BasicProfile) => {
//        Logger.debug("* converting u: BasicProfile")
//        Logger.debug(u.toString)
//        Logger.debug("u.passwordInfo")
//        Logger.debug(u.passwordInfo.toString)

        val somePassword = Some(u.passwordInfo.getOrElse(PasswordInfo("", "")).password) //(u.passwordInfo.isEmpty) //Some(u.passwordInfo.get.password)

        val retOpt = Option {
          (
            Option(0.toLong),
            u.userId,
            u.providerId,
            u.firstName.getOrElse(""),
            u.lastName.getOrElse(""),
            u.fullName.getOrElse(""),
            u.email,
            u.avatarUrl,
            u.authMethod,
            u.oAuth1Info.map(_.token),
            u.oAuth1Info.map(_.secret),
            u.oAuth2Info.map(_.accessToken),
            u.oAuth2Info.flatMap(_.tokenType),
            u.oAuth2Info.flatMap(_.expiresIn),
            u.oAuth2Info.flatMap(_.refreshToken)
            , somePassword
            //, u.oAuth2Info.flatMap(_.refreshToken)
          )
        }
//        Logger.debug("* converting opt")
//        Logger.debug(retOpt.toString)
        retOpt
      }
      })
    }
  }

}