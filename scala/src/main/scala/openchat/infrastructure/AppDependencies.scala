package openchat.infrastructure

import cats.effect.IO
import doobie.Transactor
import openchat.application.handlers._
import openchat.application.repositories.UserRepository
import openchat.application.wrapper.UuidWrapper
import openchat.domain.services.UsernameService
import openchat.infrastructure.config.AppConfig
import openchat.infrastructure.repositories.UserRepositoryDoobie
import openchat.infrastructure.wrapper.UuidWrapperNative

import java.time.Clock
import scala.concurrent.ExecutionContext

/** Simple wiring module for the application layer. This version uses configuration (AppConfig) for DB settings.
  */
final class AppDependencies(config: AppConfig)(implicit val ec: ExecutionContext) {
  // core singletons
  val clock: Clock      = Clock.systemUTC()
  val uuid: UuidWrapper = new UuidWrapperNative

  // Create a Doobie transactor from config
  private val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    driver = config.db.driver,
    url = config.db.url,
    logHandler = None
  )

  // repositories (switch to Doobie-based implementation)
  val userRepository: UserRepository = new UserRepositoryDoobie(xa)

  // services
  val usernameService: UsernameService = new UsernameService(userRepository)

  // handlers
  val userRegistrationHandler: UserRegistrationHandler =
    new UserRegistrationHandler(usernameService, userRepository, uuid)

  val loginHandler: LoginHandler = new LoginHandler(userRepository)

  val postCreationHandler: PostCreationHandler =
    new PostCreationHandler(userRepository, uuid, clock)

  val timelineQueryHandler: TimelineQueryHandler = new TimelineQueryHandler(userRepository)
}
