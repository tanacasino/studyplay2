package models

import play.api.libs.json.Json

case class User(userId: Long, name: String, isAdmin: Boolean)

object User {

  implicit val userWrites = Json.writes[User]

}
