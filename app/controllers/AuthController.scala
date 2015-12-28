package controllers

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import jp.t2v.lab.play2.auth.{ AuthElement, LoginLogout }
import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.mvc._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.driver.JdbcProfile

import auth.AuthConfigImpl
import models._
import services.UserService
import services.repositories.Tables._

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

  implicit val authDataReads = Json.reads[AuthData]

  import services.repositories.Tables.profile.api._

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
        val action: DBIO[Result] = userService.authenticate(authData.username, authData.password).flatMap { userOpt =>
          userOpt match {
            case Some(account) => DBIO.from(gotoLoginSucceeded(account.userId))
            // TODO(tanacasino): Show signin page with error form
            case None =>
              DBIO.successful(Unauthorized(Json.obj("message" -> "authentication failed")))
          }
        }
        db.run(action)
      }
    )
  }

  def profile = AsyncStack(AuthorityKey -> Normal) { implicit req =>
    val user = loggedIn
    db.run {
      userService.find(user.userId).map {
        case Some(user) => Ok(views.html.profile(user))
        case None => InternalServerError
      }
    }
  }

}

