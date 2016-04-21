package controllers.custom

import javax.inject._

import scala.concurrent.{ ExecutionContext, Future }

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.mvc._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.dbio.DBIO
import slick.driver.JdbcProfile

/** Action{Builder,Function,Refiner} を使ってみる実験 */
@Singleton
class AuthController @Inject() (
  val dbConfigProvider: DatabaseConfigProvider
)(
  implicit
  val ec: ExecutionContext
) extends Controller with HasDatabaseConfigProvider[JdbcProfile] with AuthJson with ActionComponent {

  val cache: scala.collection.mutable.Map[CacheId, CachedContent] = scala.collection.mutable.Map.empty

  val authenticationService = new AuthenticationService(new UserRepository, cache)

  def auth = Action(parse.json) { req =>
    req.body.validate[AuthRequest].fold(
      invalid => BadRequest,
      valid =>
        authenticationService.authenticate(valid.username, valid.password).map { session =>
          Ok(Json.toJson(valid)).withSession("id" -> session.cacheId.value)
        }.getOrElse(Unauthorized)
    )
  }

  def userAction = UserAction { req =>
    req.userSession match {
      case Some(session) => Ok(s"$session")
      case None => Ok("Not session")
    }
  }

}

case class CacheId(value: String)

case class CachedContent(body: String)

case class User(username: String, password: String, age: Int)

case class SessionData(cacheId: CacheId, cachedContent: CachedContent)

class UserRequest[A](val userSession: Option[SessionData], request: Request[A]) extends WrappedRequest[A](request)

class UserRepository {
  def find(username: String, withPassword: Boolean = true): Option[User] = {
    Some(User("tarou", "pass", 30))
  }
}

class AuthenticationService(userRepository: UserRepository, cache: scala.collection.mutable.Map[CacheId, CachedContent]) {

  def authenticate(username: String, plainPassword: String): Option[SessionData] = {
    userRepository
      .find(username)
      .filter(_.password == plainPassword)
      .map(_ => SessionData(CacheId(java.util.UUID.randomUUID().toString), CachedContent("content body")))
      .map { session =>
        cache += (session.cacheId -> session.cachedContent)
        session
      }
  }

}

trait ActionComponent {

  def cache: scala.collection.mutable.Map[CacheId, CachedContent]

  def UserAction = new ActionBuilder[UserRequest] with ActionRefiner[Request, UserRequest] {

    override protected def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
      Future.successful {
        Right {
          request.session.get("id").map(CacheId).flatMap { cacheId =>
            cache.get(cacheId).map { content =>
              SessionData(cacheId, content)
            }.map { session =>
              new UserRequest(Some(session), request)
            }
          }.getOrElse(new UserRequest(None, request))
        }
      }
    }
  }

}

trait AuthJson {

  case class AuthRequest(username: String, password: String)

  implicit val authRequestFormat: Format[AuthRequest] = Json.format[AuthRequest]

}
