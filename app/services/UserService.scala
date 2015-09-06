package services

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.mvc._
import slick.driver.JdbcProfile

import models.User
import services.repositories.Tables._


class UserService @Inject() (
    val dbConfigProvider: DatabaseConfigProvider
)(
    implicit
    ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import dbConfig.driver.api._

  def create(name: String, password: String, isAdmin: Boolean = false): Future[UsersRow] = db.run {
    (
      Users.map(u => (u.name, u.password, u.isAdmin))
      returning Users.map(_.userId)
      into ((u, userId) => UsersRow(userId, u._1, u._2, u._3))
    ) += ((name, password, isAdmin))
  }

  def list(): Future[Seq[UsersRow]] = db.run {
    Users.result
  }

  def find(userId: Long): Future[Option[User]] = db.run {
    Users
      .filter(_.userId === userId.bind)
      .result
      .headOption
  }.map(toUserOpt)

  def authenticate(username: String, password: String) = db.run {
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

