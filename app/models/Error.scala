package models

import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.libs.json._

/**
  * Represent Application Error
  * Convert to Http Result
  */
sealed trait Error {

  def toResult: Result = this match {
    case e: InvalidRequest =>
      BadRequest(Json.toJson(e))
    case e: ResourceNotFound =>
      NotFound(Json.toJson(e))
    case e: ResourceConflict =>
      Conflict(Json.toJson(e))
    case e: PermissionDenied =>
      Forbidden(Json.toJson(e))
    case e: UnauthorizeError =>
      Unauthorized(Json.toJson(e))
    case e: UnexpectedError =>
      InternalServerError(Json.toJson(e))
  }

  implicit val errorWrites = new OWrites[Error] {
    def writes(o: Error): JsObject = o match {
      case InvalidRequest(errors) =>
        errorJson(code = "error.invalid", msg = "Invalid Request") ++ Json.obj(
          "errors" -> JsArray(
            errors.map { error =>
              Json.obj(
                "path" -> error.path,
                "code" -> error.code
              )
            }
          )
        )
      case e: ResourceNotFound =>
        errorJson(e.code, e.msg)
      case e: ResourceConflict =>
        errorJson(e.code, e.msg)
      case e: PermissionDenied =>
        errorJson(e.code, e.msg)
      case e: UnauthorizeError =>
        errorJson(e.code, e.msg)
      case e: UnexpectedError =>
        errorJson(e.code, e.msg)
    }
  }

  private def errorJson(code: String, msg: String): JsObject = {
    Json.obj(
      "code" -> code,
      "msg" -> msg
    )
  }

}

/**
  * Error factory
  */
object Error {

  def apply(jsError: JsError): Error = {
    val errors = for {
      (path, verrors) <- jsError.errors
      verror <- verrors
    } yield InvalidParameter(path.toString, verror.message)
    InvalidRequest(errors)
  }

}

/**
  * Error types
  */
final case class InvalidParameter(path: String, code: String)

final case class InvalidRequest(errors: Seq[InvalidParameter]) extends Error

final case class ResourceNotFound(
  code: String = "error.resource.notFound",
  msg: String = "Specified resource is not found."
) extends Error

final case class ResourceConflict(
  code: String = "",
  msg: String = ""
) extends Error

final case class PermissionDenied(
  code: String = "error.permissionDenied",
  msg: String = "You cannot access."
) extends Error

final case class UnauthorizeError(
  code: String = "error.unauthorize",
  msg: String = "Unauthorize"
) extends Error

final case class UnexpectedError(
  code: String = "error.unexpected",
  msg: String = "Internal Server Error"
) extends Error
