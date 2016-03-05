package controllers

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import jp.t2v.lab.play2.auth.{ AuthElement, LoginLogout }
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.mvc._
import slick.driver.JdbcProfile

import auth.{ AuthComponent, AuthConfigImpl }
import models.Admin

class AdminController @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val component: AuthComponent
)(
  implicit
  val ec: ExecutionContext
) extends Controller with AuthElement with AuthConfigImpl {

  def index = AsyncStack(AuthorityKey -> Admin) { implicit req =>
    Future.successful(Ok("Admin Page"))
  }

}

