package utils

import models.Todo
import models.User
import play.Environment
import play.Logger
import javax.inject.Inject
import javax.inject.Singleton

import play.api.Logger

@Singleton
class DemoData @Inject()(environment: Environment){
  if (environment.isDev || environment.isTest) {
    if (User.findByEmailAddressAndPassword ("user1@demo.com", "password") == null) {
      play.Logger.info ("Loading Demo Data")
    /*  user1 = new User ("user1@demo.com", "password", "John Doe")
      user1.save ()
      todo1_1 = new Todo(user1, "make it secure")
      todo1_1.save ()
      todo1_2 = new Todo (user1, "make it neat")
      todo1_2.save ()
      user2 = new User ("user2@demo.com", "password", "Jane Doe")
      user2.save ()
      todo2_1 = new Todo (user2, "make it pretty")
      todo2_1.save ()*/

      todo2_1 = new Todo
      todo2_1.user = user1
      todo2_1.value = "value1"
      todo2_1.save ()
    }
  }
  var user1: User = null
  var user2: User = null
  var todo1_1: Todo = null
  var todo1_2: Todo = null
  var todo2_1: Todo = null
}

/*
@Singleton
class DemoData @Inject(environment: Environment) (){
  if (environment.isDev || environment.isTest) {
  if (User.findByEmailAddressAndPassword ("user1@demo.com", "password") == null) {
  Logger.info ("Loading Demo Data")
  user1 = new User ("user1@demo.com", "password", "John Doe")
  user1.save ()
  todo1_1 = new Todo (user1, "make it secure")
  todo1_1.save ()
  todo1_2 = new Todo (user1, "make it neat")
  todo1_2.save ()
  user2 = new User ("user2@demo.com", "password", "Jane Doe")
  user2.save ()
  todo2_1 = new Todo (user2, "make it pretty")
  todo2_1.save ()
}
}
  var user1: User = null
  var user2: User = null
  var todo1_1: Todo = null
  var todo1_2: Todo = null
  var todo2_1: Todo = null
}*/