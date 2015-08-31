package controllers

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.mvc._

import services.UserService
import services.repositories.Tables._

class UserController @Inject() (service: UserService)(implicit ec: ExecutionContext) extends Controller {

  val r = new Random

  implicit val usersRowWrites = new Writes[UsersRow] {
    def writes(user: UsersRow): JsValue = {
      Json.obj(
        "userId" -> JsNumber(user.userId),
        "name" -> JsString(user.name),
        "isAdmin" -> JsBoolean(user.isAdmin)
      )
    }
  }

  def list = Action.async {
    service.list map { users =>
      Ok(Json.toJson(users))
    }
  }

  def test = Action.async {
    Future.sequence(List.fill(5)(r.nextInt).map(u => service.create(s"user-$u", "password"))) map { users =>
      Ok(Json.toJson(users))
    }
  }

}

