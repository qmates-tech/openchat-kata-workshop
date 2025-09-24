package openchat.infrastructure.api

import openchat.application.handlers.{LoginHandler, LoginUser}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

final class LoginRoutes(login: LoginHandler)(implicit ec: ExecutionContext) extends JsonProtocols {

  val route: Route =
    // POST /login
    path("login") {
      post {
        entity(as[LoginUser]) { loginUser =>
          onComplete(login.handle(loginUser)) {
            case Success(Some(u)) => complete(StatusCodes.OK -> u)
            case Success(None)    => complete(StatusCodes.Unauthorized -> ErrorMessage("Invalid credentials."))
            case Failure(_)       => complete(StatusCodes.InternalServerError -> ErrorMessage("Internal server error"))
          }
        }
      }
    }
}
