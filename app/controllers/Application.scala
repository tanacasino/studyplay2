package controllers

import javax.inject.Inject

import play.api._
import play.api.mvc._

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import slick.driver.JdbcProfile


class Application @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends Controller with HasDatabaseConfigProvider[JdbcProfile] {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}
