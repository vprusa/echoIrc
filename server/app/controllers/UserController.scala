package controllers

import java.util.concurrent.TimeoutException
import javax.inject.Singleton

import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{ignored, mapping}
import play.api.libs.json.Json


//

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

// http://stackoverflow.com/questions/37371698/could-not-find-implicit-value-for-parameter-messages-play-api-i18n-messages-in

/**
  * A very simple chat client using websockets.
  */
@Singleton
class UserController @Inject()(implicit actorSystem: ActorSystem, webJarAssets: WebJarAssets,
                                   mat: Materializer,
                                   executionContext: ExecutionContext, override val messagesApi: MessagesApi)
  extends BaseController with I18nSupport {

  implicit val empJsonFormat = Json.format[User]

  /**
    * Describe the user form (used in both edit and create screens).
    */
  val userForm = Form(
    mapping(
      "id" -> ignored(0: Long),
      "name" -> nonEmptyText,
      "address" -> nonEmptyText,
      "designation" -> nonEmptyText)(User.apply)(User.unapply))

  /**
    * This result directly redirect to the application home.
    */
  val Home = Redirect(routes.UserController.list())


  /**
    * Display the list of users.
    *
    */
  def list = Action.async { implicit request =>
    val futurePage: Future[List[User]] = TimeoutFuture(User.findAll)
    futurePage.map(users => Ok(Json.toJson(users))).recover {
      case t: TimeoutException =>
        Logger.error("Problem found in user list process")
        InternalServerError(t.getMessage)
    }
  }

  /**
    * Display the 'edit form' of a existing User.
    *
    * @param id Id of the user to edit
    */
  def edit(id: Long) = Action.async {
    val futureEmp: Future[Option[models.User]] = TimeoutFuture(User.findById(id))
    futureEmp.map {
      case Some(user) => Ok("")
      case None => NotFound
    }.recover {
      case t: TimeoutException =>
        Logger.error("Problem found in user edit process")
        InternalServerError(t.getMessage)
    }
  }

  /**
    * Handle the 'edit form' submission
    *
    * @param id Id of the user to edit
    */
  def update(id: Long) = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest("")),
      user => {
        val futureUpdateEmp: Future[Int] = TimeoutFuture(User.update(id, user))
        futureUpdateEmp.map { empId =>
          Home.flashing("success" -> s"User ${user.name} has been updated")
        }.recover {
          case t: TimeoutException =>
            Logger.error("Problem found in user update process")
            InternalServerError(t.getMessage)
        }
      })
  }

  /**
    * Handle the 'new user form' submission.
    */
  def save = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest("")),
      user => {
        val futureUpdateEmp: Future[Option[Long]] = TimeoutFuture(User.insert(user))
        futureUpdateEmp.map {
          case Some(empId) =>
            val msg = s"User ${user.name} has been created"
            Logger.info(msg)
            Home.flashing("success" -> msg)
          case None =>
            val msg = s"User ${user.name} has not created"
            Logger.info(msg)
            Home.flashing("error" -> msg)
        }.recover {
          case t: TimeoutException =>
            Logger.error("Problem found in user update process")
            InternalServerError(t.getMessage)
        }
      })
  }

  /**
    * Handle user deletion.
    */
  def delete(id: Long) = Action.async {
    val futureInt = TimeoutFuture(User.delete(id))
    futureInt.map(i => Home.flashing("success" -> "User has been deleted")).recover {
      case t: TimeoutException =>
        Logger.error("Problem deleting user")
        InternalServerError(t.getMessage)
    }
  }


}
