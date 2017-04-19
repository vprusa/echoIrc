package controllers

import models.Todo
import play.data.Form
import play.data.FormFactory
import play.mvc.Controller
import play.mvc.Result
import play.mvc.Security
import javax.inject.Inject
import play.libs.Json.toJson

@Security.Authenticated(classOf[Secured]) class TodoController extends Controller {
  @Inject private[controllers] var formFactory: FormFactory = null

  def getAllTodos: Result = play.mvc.Results.ok(toJson(Todo.findByUser(SecurityController.getUser)))

  def createTodo: Result = {
    val form = formFactory.form(classOf[Todo]).bindFromRequest()
    if (form.hasErrors) play.mvc.Results.badRequest(form.errorsAsJson)
    else {
      val todo : Todo = form.get
      todo.user = SecurityController.getUser
      todo.save
      play.mvc.Results.ok(toJson(todo))
    }
  }
}