package models

import com.avaje.ebean.Model
import com.fasterxml.jackson.annotation.JsonIgnore
import play.data.validation.Constraints
import javax.persistence._
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util
import java.util.Date
import java.util.UUID

@Entity object User {
  def getSha512(value: String): Array[Byte] = try MessageDigest.getInstance("SHA-512").digest(value.getBytes("UTF-8"))
  catch {
    case e@(_: NoSuchAlgorithmException | _: UnsupportedEncodingException) =>
      throw new RuntimeException(e)
  }

  var find = new Model.Finder[Long, User](classOf[User])

  def findByAuthToken(authToken: String): User = {
    if (authToken == null) return null
    try find.where.eq("authToken", authToken).findUnique
    catch {
      case e: Exception =>
        null
    }
  }

  def findByEmailAddressAndPassword(emailAddress: String, password: String): User = { // todo: verify this query is correct.  Does it need an "and" statement?
    find.where.eq("emailAddress", emailAddress.toLowerCase).eq("shaPassword", getSha512(password)).findUnique
  }
}

@Entity class User() extends Model {
  this.creationDate = new Date() //= new Date()

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) var id = 0L
  private var authToken: String = null
  @Column(length = 256, unique = true, nullable = false)
  @Constraints.MaxLength(256)
  @Constraints.Required
  @Constraints.Email private var emailAddress: String = null

  def getEmailAddress: String = emailAddress

  def setEmailAddress(emailAddress: String) = {
    this.emailAddress = emailAddress.toLowerCase
  }

  @Column(length = 64, nullable = false) private var shaPassword: String = null
  @Transient
  @Constraints.Required
  @Constraints.MinLength(6)
  @Constraints.MaxLength(256)
  @JsonIgnore private var password: String = null

  def getPassword: String = password

  def setPassword(password: String): Unit = {
    this.password = password
    // TODO problem?
    shaPassword = User.getSha512(password).toString
  }

  @Column(length = 256, nullable = false)
  @Constraints.Required
  @Constraints.MinLength(2)
  @Constraints.MaxLength(256) var fullName: String = null
  @Column(nullable = false) var creationDate: Date = null

  def createToken: String = {
    authToken = UUID.randomUUID.toString
    save()
    authToken
  }

  def deleteAuthToken(): Unit = {
    authToken = null
    save()
  }

  @OneToMany(cascade = Array(CascadeType.ALL), mappedBy = "user")
  @JsonIgnore var todos = new util.ArrayList[Todo]

  def this(emailAddress: String, password: String, fullName: String) {
    this()
    setEmailAddress(emailAddress)
    setPassword(password)
    this.fullName = fullName
    this.creationDate = new Date
  }
}