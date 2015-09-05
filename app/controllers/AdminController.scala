package controllers

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import jp.t2v.lab.play2.auth.{ AuthElement, LoginLogout }
import play.api.mvc._

import auth._
import services.UserService

class AdminController @Inject() (
    val userService: UserService
)(
    implicit
    ec: ExecutionContext
) extends Controller with AuthElement with AuthConfigImpl {

  def index = AsyncStack(AuthorityKey -> Admin) { implicit req =>
    Future.successful(Ok("Admin Page"))
  }

}
