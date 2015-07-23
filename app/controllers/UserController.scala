package controllers


import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.mvc._

import services.UserService
import models.User


class UserController @Inject()(service: UserService)(implicit ec: ExecutionContext) extends Controller {

  val r = new Random

  implicit val userWrites = Json.writes[User]


  def list = Action.async {
    service.list map { users =>
      Ok(Json.toJson(users))
    }
  }


  def test = Action.async {
    Future.sequence(List.fill(5)(r.nextInt).map(u => service.create(s"user-$u", r.nextInt))) map { users =>
      Ok(users.toString)
    }
  }

}


