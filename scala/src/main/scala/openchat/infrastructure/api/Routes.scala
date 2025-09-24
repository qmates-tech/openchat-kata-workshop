package openchat.infrastructure.api

import openchat.application.handlers._
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

final class Routes(
  userRegistration: UserRegistrationHandler,
  login: LoginHandler,
  postCreation: PostCreationHandler,
  timelineQuery: TimelineQueryHandler
                  )(implicit ec: ExecutionContext) {

  private val userRoutes = new UserRoutes(userRegistration, postCreation, timelineQuery)
  private val loginRoutes = new LoginRoutes(login)

  val route: Route = userRoutes.route ~ loginRoutes.route
}
