package services

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }
import slick.dbio.DBIO

import models.User
import repositories.jdbc.UserRepository

// TODO(tanacasino): Serviceをトランザクション境界として serviceで db.run しちゃうでもいいかも。全部DBIOになっちゃうよ
class UserService @Inject() (userRepository: UserRepository)(implicit val ec: ExecutionContext) {

  // TODO(tanacasino): パスワードをハッシュ化する。jbcrypto
  def create(name: String, password: String, isAdmin: Boolean = false): DBIO[User] = {
    userRepository.create(
      name = name,
      password = password,
      isAdmin = isAdmin
    )
  }

  def list(): DBIO[Seq[User]] = {
    userRepository.list
  }

  def find(userId: Long): DBIO[Option[User]] = {
    userRepository.find(userId)
  }

  // TODO(tanacasino): パスワードをハッシュ化する。jbcrypto
  def authenticate(username: String, password: String): DBIO[Option[User]] = {
    userRepository.authenticate(username, password)
  }

}

