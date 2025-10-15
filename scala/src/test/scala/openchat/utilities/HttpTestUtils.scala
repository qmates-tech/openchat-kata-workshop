package openchat.utilities

import openchat.infrastructure.api.JsonProtocols
import org.scalatest.Assertions._
import spray.json._
import sttp.client4._
import sttp.client4.httpclient.HttpClientSyncBackend

object HttpTestUtils extends JsonProtocols {
  val backend: WebSocketSyncBackend = HttpClientSyncBackend()
  val BASE_URI                      = "http://localhost:8080"

  def generateRandomSuffix: String = {
    java.util.UUID.randomUUID().toString.take(8)
  }

  def parseJson(response: Response[Either[String, String]]): JsValue = {
    response.body match {
      case Right(jsonString) => jsonString.parseJson
      case Left(jsonString)  => jsonString.parseJson
    }
  }

  def extractJsonField(response: Response[Either[String, String]], field: String): String = {
    response.body.fold(
      err => fail(s"Failed to extract field from JSON: $err"),
      _.parseJson.asJsObject.fields(field).convertTo[String]
    )
  }

  def parseJsonArray(response: Response[Either[String, String]]): List[JsValue] = {
    response.body.fold(
      err => fail(s"Failed to parse JSON array: $err"),
      _.parseJson.convertTo[List[JsValue]]
    )
  }
}
