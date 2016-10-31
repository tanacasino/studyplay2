package controllers

import akka.util.ByteString
import play.api.http.{ HttpEntity, Writeable }
import play.api.libs.json.Json
import play.api.mvc._

class SampleController extends Controller {

  def helloText = Action { req =>
    Ok("Hello, World!")
  }

  def helloText2 = Action { req =>
    val writeable = Writeable((a: String) => ByteString(a), Some("text/plain"))
    new Status(200).apply("Hello, World!")(writeable = writeable)
  }

  def helloText3 = Action { req =>
    Result(
      header = ResponseHeader(status = 200),
      body = HttpEntity.Strict(
        data = ByteString("Hello, World!"),
        contentType = Some("text/plain")
      )
    )
  }

  def helloXml = Action { req =>
    Ok(<message>Hello, World!</message>)
  }

  def helloJson = Action { req =>
    Ok(Json.obj("message" -> "Hello, World!"))
  }

}
