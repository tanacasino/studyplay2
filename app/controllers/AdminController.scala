package controllers

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import jp.t2v.lab.play2.auth.{ AuthElement, LoginLogout }
import play.api.mvc._

import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.driver.JdbcProfile

import auth.AuthConfigImpl
import models._
import services.UserService

class AdminController @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val userService: UserService
)(
  implicit
  val ec: ExecutionContext
) extends Controller
  with AuthElement
  with AuthConfigImpl
  with HasDatabaseConfigProvider[JdbcProfile] {

  def index = AsyncStack(AuthorityKey -> Admin) { implicit req =>
    Future.successful(Ok("Admin Page"))
  }

}
