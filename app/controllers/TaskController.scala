package controllers

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import jp.t2v.lab.play2.auth.AuthElement
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.mvc._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.dbio.DBIO
import slick.driver.JdbcProfile

import auth.{ AuthComponent, AuthConfigImpl }
import models._
import services._

class TaskController @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val component: AuthComponent,
  val taskService: TaskService
)(
  implicit
  val ec: ExecutionContext
) extends Controller with AuthElement with AuthConfigImpl {

  def updateTask(taskId: Long) = AsyncStack(AuthorityKey -> Normal) { implicit req =>
    val user = loggedIn
    db.run {
      taskService.updateTask(taskId, "name", user, 1L).run
    }
      .map(_.fold(_.toResult, task => Ok("OK")))
  }

}
