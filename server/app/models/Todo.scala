package models

import com.avaje.ebean.Model
import com.fasterxml.jackson.annotation.JsonIgnore
import play.data.validation.Constraints
import javax.persistence._
import java.util
import java.util.List

@Entity object Todo {
  def findByUser(user: User): util.List[Todo] = {
    val finder = new Model.Finder[Long, Todo](classOf[Todo])
    finder.where.eq("user", user).findList
  }
}

/*
@Entity class Todo(var user: User, var value: String) extends Model {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) var id = 0L

}*/

@Entity
class Todo extends Model {

  @ManyToOne
  @JsonIgnore
  var user: User = null

  @Column(length = 1024, nullable = false)
  @Constraints.MaxLength(1024)
  @Constraints.Required
  var value: String = null

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) var id = 0L

}

/*
@Entity
class Todo extends Model {
  @ManyToOne
  @JsonIgnore
  var _user: User

  @Column(length = 1024, nullable = false)
  @Constraints.MaxLength(1024)
  @Constraints.Required
  var _value: String

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) var _id = 0L

  def id = _id // accessor
  def id_=(aId: Long) {
    _id = aId
  } // mutator

  def value = _value // accessor
  def value_=(aValue: String) {
    _value = aValue
  } // mutator

  def user = _user // accessor
  def user_=(aUser: User) {
    _user = aUser
  } // mutator

}
*/
/*
@Entity class Todo() extends Model {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) var id = 0L

  @Column(length = 1024, nullable = false)
  @Constraints.MaxLength(1024)
  @Constraints.Required var value = null

  @ManyToOne
  @JsonIgnore var user = null
}
*/
