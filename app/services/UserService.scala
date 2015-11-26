package services

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import models.User
import services.repositories.Tables._, profile.api._

class UserService @Inject() (implicit val ec: ExecutionContext) {

  def create(name: String, password: String, isAdmin: Boolean = false): DBIO[User] = {
    (
      Users.map(u => (u.name, u.password, u.isAdmin))
      returning Users.map(_.userId)
      into ((u, userId) => UsersRow(userId, u._1, u._2, u._3))
    ) += ((name, password, isAdmin))
  }.map(toUser)


  def insertExamples(name: String, password: String, isAdmin: Boolean = false): DBIO[UsersRow] = {
    // 例えばこんなイメージのスキーマに対して
    // case class UsersRow(userId: Long, name: String, password: String, isAdmin: Boolean)

    // PK auto increment を指定せずにinsertする
    // DBIO[Int] (insert件数)が返却される
    Users
      .map(u => (u.name, u.password, u.isAdmin)) += ((name, password, isAdmin))


    // DBIO[Long] (pk件数)が返却されるようにする
    Users
      .map(u => (u.name, u.password, u.isAdmin))
      .returning(Users.map(_.userId)) += ((name, password, isAdmin))


    // DBIO[UsersRow] が返却されるようにする
    // 内部的には、DBサーバからはPKしか帰ってこないので、返ってきたPKと += で渡したタプルを使ってUsersRowを作る
    Users
      .map(u => (u.name, u.password, u.isAdmin))
      .returning(Users.map(_.userId))
      .into { (u, userId) =>
        // u は、+= で渡す予定のname/password/isAdminのタプル
        UsersRow(userId, u._1, u._2, u._3)
      } += ((name, password, isAdmin))
  }


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


  private def toUser(user: UsersRow): User = {
    User(
      userId = user.userId,
      name = user.name,
      isAdmin = user.isAdmin
    )
  }

  lazy val toUserOpt = lift(toUser)

  private def lift[A, B](f: A => B): Option[A] => Option[B] = _ map f

}

