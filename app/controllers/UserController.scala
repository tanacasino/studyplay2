package controllers

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.mvc._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.dbio.DBIO
import slick.driver.JdbcProfile

import models.User
import services.UserService

class UserController @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val userService: UserService
)(implicit val ec: ExecutionContext) extends Controller with HasDatabaseConfigProvider[JdbcProfile] {

  val r = new Random

  def list = Action.async {
    val action: DBIO[Result] = userService.list map { users =>
      Ok(Json.toJson(users))
    }
    db.run(action)
  }

  def test = Action.async {
    Future.sequence(List.fill(5)(r.nextInt).map(u => createUser(s"user-$u", "password"))) map { users =>
      Ok(Json.toJson(users))
    }
  }

  private def createUser(user: String, pass: String): Future[User] = db.run {
    userService.create(user, pass)
  }

}

