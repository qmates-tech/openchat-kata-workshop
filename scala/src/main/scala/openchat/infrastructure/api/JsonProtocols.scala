package openchat.infrastructure.api

import openchat.application.handlers._
import openchat.domain.commands.CreateUser
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

// DTOs for HTTP layer where domain types don't exactly match request/response shapes
final case class CreatePostRequest(text: String)
final case class ErrorMessage(message: String)

trait JsonProtocols extends SprayJsonSupport with DefaultJsonProtocol {
  import spray.json.RootJsonFormat

  // Requests
  implicit val createUserFormat: RootJsonFormat[CreateUser]           = jsonFormat3(CreateUser)
  implicit val loginUserFormat: RootJsonFormat[LoginUser]             = jsonFormat2(LoginUser)
  implicit val createPostReqFormat: RootJsonFormat[CreatePostRequest] = jsonFormat1(CreatePostRequest)

  // Responses
  implicit val userCreatedFormat: RootJsonFormat[UserCreated]           = jsonFormat3(UserCreated)
  implicit val userLoggedInFormat: RootJsonFormat[UserLoggedIn]         = jsonFormat3(UserLoggedIn)
  implicit val postCreatedFormat: RootJsonFormat[PostCreated]           = jsonFormat4(PostCreated)
  implicit val timelinePostViewFormat: RootJsonFormat[TimelinePostView] = jsonFormat4(TimelinePostView)
  implicit val errorMessageFormat: RootJsonFormat[ErrorMessage]         = jsonFormat1(ErrorMessage)
}
