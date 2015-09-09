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

import auth.AuthConfigImpl
import models._
import services.UserService
import services.repositories.Tables._

case class AuthData(username: String, password: String)

class AuthController @Inject() (
    val userService: UserService,
    val messagesApi: MessagesApi
)(
    implicit
    ec: ExecutionContext
) extends Controller with AuthElement with LoginLogout with AuthConfigImpl with I18nSupport {

  val logger = Logger(this.getClass)

  implicit val authDataReads = Json.reads[AuthData]

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
        userService.authenticate(authData.username, authData.password).flatMap { userOpt =>
          userOpt match {
            case Some(account) => gotoLoginSucceeded(account.userId)
            // TODO(tanacasino): Show signin page with error form
            case None => Future.successful(Unauthorized(Json.obj("message" -> "authentication failed")))
          }
        }
      }
    )
  }

  def profile = AsyncStack(AuthorityKey -> Normal) { implicit req =>
    val user = loggedIn
    userService.find(user.userId).map {
      case Some(user) => Ok(views.html.profile(user))
      case None => InternalServerError
    }
  }

}

