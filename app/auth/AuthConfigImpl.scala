package auth

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.reflect.{ClassTag,classTag}

import play.api.mvc._
import play.api.mvc.Results._
import jp.t2v.lab.play2.auth.AuthConfig

import services.UserService

// Account
case class Account(userId: Long, name: String, isAdmin: Boolean)

// Roles
sealed trait Role
case object Admin extends Role
case object Normal extends Role

trait AuthConfigImpl extends AuthConfig {

  val userService: UserService

  type Id = Long

  type User = Account

  type Authority = Role

  val idTag: ClassTag[Id] = classTag[Id]

  override def sessionTimeoutInSeconds: Int = 3600

  override def resolveUser(id: Id)(implicit context: ExecutionContext): Future[Option[User]] = {
    userService.find(id)
  }

  override def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect("/"))
  }

  override def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect("/"))
  }

  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Unauthorized)
  }

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Forbidden)
  }

  override def authorize(user: User, authority: Authority)(implicit context: ExecutionContext): Future[Boolean] = Future.successful {
    (user.isAdmin, authority) match {
      case (true, _) => true
      case (false, Normal) => true
      case _ => false
    }
  }

}
