package services

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import models.User
import services.repositories.Tables._; import profile.api._

class UserService @Inject() (implicit val ec: ExecutionContext) {

  def create(name: String, password: String, isAdmin: Boolean = false): DBIO[User] = {
    (
      Users.map(u => (u.name, u.password, u.isAdmin))
      returning Users.map(_.userId)
      into ((u, userId) => UsersRow(userId, u._1, u._2, u._3))
    ) += ((name, password, isAdmin))
  }.map(toUser)

  def list(): DBIO[Seq[User]] = {
    Users.result
  }.map(_.map(toUser))

  def find(userId: Long): DBIO[Option[User]] = {
    Users
      .filter(_.userId === userId.bind)
      .result
      .headOption
  }.map(toUserOpt)

  def authenticate(username: String, password: String): DBIO[Option[User]] = {
    Users
      .filter(_.name === username.bind)
      .filter(_.password === password.bind)
      .result
      .headOption
  }.map(toUserOpt)

  lazy val toUserOpt = lift(toUser)

  private def toUser(user: UsersRow): User = {
    User(
      userId = user.userId,
      name = user.name,
      isAdmin = user.isAdmin
    )
  }

  private def lift[A, B](f: A => B): Option[A] => Option[B] = _ map f

}

