package repositories.jdbc

import javax.inject.Inject

import scala.concurrent.ExecutionContext

import models.User
import repositories.Tables._, profile.api._

class UserRepository @Inject() ()(implicit val ec: ExecutionContext) {

  def create(name: String, password: String, isAdmin: Boolean): DBIO[User] = {
    val action: DBIO[UsersRow] = (
      Users.map(u => (u.name, u.password, u.isAdmin))
      returning Users.map(_.userId)
      into ((u, userId) => UsersRow(userId, u._1, u._2, u._3))
    ) += ((name, password, isAdmin))
    action.map(toUser)
  }

  def list(): DBIO[Seq[User]] = {
    Users
      .result
      .map(_.map(toUser))
  }

  def find(userId: Long): DBIO[Option[User]] = {
    Users
      .filter(_.userId === userId.bind)
      .result
      .headOption
      .map(_.map(toUser))
  }

  def authenticate(username: String, password: String): DBIO[Option[User]] = {
    Users
      .filter(_.name === username.bind)
      .filter(_.password === password.bind)
      .result
      .headOption
      .map(_.map(toUser))
  }

  private def toUser(user: UsersRow): User = {
    User(
      userId = user.userId,
      name = user.name,
      isAdmin = user.isAdmin
    )
  }

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

}
