package controllers

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import slick.driver.JdbcProfile

class Application @Inject() (val dbConfigProvider: DatabaseConfigProvider) extends Controller with HasDatabaseConfigProvider[JdbcProfile] {

  import dbConfig.driver.api._

  val query = sql"""SELECT 1"""

  def index = Action.async {
    db.run(query.as[Int].headOption) map { result =>
      val status = result.getOrElse(0)
      Ok(views.html.index(s"Your new application is ready. status=$status"))
    }
  }

}
