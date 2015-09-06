package auth

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.reflect.{ ClassTag, classTag }

import play.api.mvc._
import play.api.mvc.Results._
import play.api.Logger
import jp.t2v.lab.play2.auth._

import services.UserService

trait AuthConfigImpl extends AuthConfig {

  val authLogger = Logger("auth")

  val userService: UserService

  type Id = Long

  type User = models.User

  type Authority = models.Role

  val idTag: ClassTag[Id] = classTag[Id]

  // TODO configuration file
  override def sessionTimeoutInSeconds: Int = 3600

  override def resolveUser(id: Id)(implicit context: ExecutionContext): Future[Option[User]] = {
    authLogger.info(s"resolveUser: $id")
    userService.find(id)
  }

  override def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect("/"))
  }

  override def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect("/"))
  }

  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    authLogger.info(s"authenticationFailed: $request")
    Future.successful(Unauthorized)
  }

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    authLogger.info(s"authorizationFailed: $request")
    Future.successful(Forbidden)
  }

  override def authorize(user: User, authority: Authority)(implicit context: ExecutionContext): Future[Boolean] = Future.successful {
    authLogger.info(s"authorize: $user, $authority")
    (user.isAdmin, authority) match {
      case (true, _) => true
      case (false, models.Normal) => true
      case _ => false
    }
  }

  override lazy val tokenAccessor = new CookieTokenAccessor(
    // TODO configuration file
    cookieName = "SESS_ID",
    cookieSecureOption = play.api.Play.isProd(play.api.Play.current),
    cookieMaxAge       = Some(sessionTimeoutInSeconds)
  )

}

