package services


import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.mvc._
import slick.driver.JdbcProfile

import models.User


class UserService @Inject()(
  val dbConfigProvider: DatabaseConfigProvider
)(
  implicit ec: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import dbConfig.driver.api._


  private class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def age = column[Int]("age")

    def * = (id, name, age) <> ((User.apply _).tupled, User.unapply)
  }


  private val Users = TableQuery[UsersTable]


  def create(name: String, age: Int): Future[User] = db.run {
    (Users.map(u => (u.name, u.age))
      returning Users.map(_.id)
      into ((nameAge, id) => User(id, nameAge._1, nameAge._2))
    ) += ((name, age))
  }


  def list(): Future[Seq[User]] = db.run {
    Users.result
  }

}

