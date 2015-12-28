package controllers

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import jp.t2v.lab.play2.auth.{ AuthElement, LoginLogout }
import play.api.Logger
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.i18n._
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc._
import slick.dbio.DBIO
import slick.driver.JdbcProfile

import auth.AuthConfigImpl
import models._
import services.UserService

case class AuthData(username: String, password: String)

class AuthController @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val messagesApi: MessagesApi,
  val userService: UserService
)(
  implicit
  val ec: ExecutionContext
) extends Controller with AuthElement with LoginLogout with AuthConfigImpl with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

  val logger = Logger(this.getClass)

  val authForm: Form[AuthData] = Form {
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(AuthData.apply)(AuthData.unapply)
  }

  def signinPage = Action { implicit req =>
    Ok(views.html.signin(authForm))
  }

  def signin() = Action.async { implicit req =>
    authForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.signin(errorForm)))
      },
      authData => {
        db.run(userService.authenticate(authData.username, authData.password)).flatMap {
          case Some(account) =>
            gotoLoginSucceeded(account.userId)
          case None =>
            // TODO(tanacasino): Show signin page with error form
            Future.successful(
              Unauthorized(
                Json.obj(
                  "message" -> "authentication failed"
                )
              )
            )
        }
      }
    )
  }

  def profile = AsyncStack(AuthorityKey -> Normal) { implicit req =>
    val user = loggedIn
    db.run(userService.find(user.userId)).map {
      case Some(user) => Ok(views.html.profile(user))
      case None => InternalServerError
    }
  }

}

