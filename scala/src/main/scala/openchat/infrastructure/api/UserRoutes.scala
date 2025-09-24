package openchat.infrastructure.api

import openchat.application.handlers._
import openchat.application.wrapper.UuidWrapper.isValidUuid
import openchat.domain.commands.{CreatePost, CreateUser}
import openchat.domain.errors.{InvalidPost, UserNotFound, UsernameAlreadyExists}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

final class UserRoutes(
  userRegistration: UserRegistrationHandler,
  postCreation: PostCreationHandler,
  timelineQuery: TimelineQueryHandler
)(implicit ec: ExecutionContext)
    extends JsonProtocols {

  val route: Route =
    pathPrefix("users") {
      // POST /users -> create user
      pathEndOrSingleSlash {
        post {
          entity(as[CreateUser]) { cmd =>
            onComplete(userRegistration.handle(cmd)) {
              case Success(Right(created)) => complete(StatusCodes.Created -> created)
              case Success(Left(UsernameAlreadyExists)) =>
                complete(StatusCodes.BadRequest -> ErrorMessage("Username already in use"))
              case Failure(_) => complete(StatusCodes.InternalServerError -> ErrorMessage("Internal server error"))
            }
          } ~
            // method not allowed if not POST
            reject
        }
      } ~
        // /users/{userId}/timeline
        path(Segment / "timeline") { userId =>
          get {
            if (!isValidUuid(userId)) complete(StatusCodes.NotFound -> ErrorMessage("User does not exist."))
            else
              onComplete(timelineQuery.handle(userId)) {
                case Success(posts)                   => complete(StatusCodes.OK -> posts)
                case Failure(_: TimelineUserNotFound) =>
                  complete(StatusCodes.NotFound -> ErrorMessage("User does not exist."))
                case Failure(_) => complete(StatusCodes.InternalServerError -> ErrorMessage("Internal server error"))
              }
          } ~
            post {
              entity(as[CreatePostRequest]) { body =>
                if (!isValidUuid(userId)) complete(StatusCodes.NotFound -> ErrorMessage("User does not exist."))
                else if (body.text == null || body.text.trim.isEmpty)
                  complete(StatusCodes.BadRequest -> ErrorMessage("Text is required."))
                else {
                  val cmd = CreatePost(userId = userId, text = body.text)
                  onComplete(postCreation.handle(cmd)) {
                    case Success(Right(created)) => complete(StatusCodes.Created -> created)
                    case Success(Left(UserNotFound)) =>
                      complete(StatusCodes.NotFound -> ErrorMessage("User does not exist."))
                    case Success(Left(InvalidPost)) =>
                      complete(StatusCodes.BadRequest -> ErrorMessage("Post contains inappropriate language"))
                    case Failure(_) =>
                      complete(StatusCodes.InternalServerError -> ErrorMessage("Internal server error"))
                  }
                }
              }
            }
        }
    }
}
