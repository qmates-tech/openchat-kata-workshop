package openchat.application.handlers

import openchat.application.Handler
import openchat.application.repositories.UserRepository
import openchat.application.wrapper.UuidWrapper
import openchat.domain.aggregates.User
import openchat.domain.commands.CreateUser
import openchat.domain.errors.{UserRegistrationError, UsernameAlreadyExists}
import openchat.domain.services.UsernameService

import scala.concurrent.{ExecutionContext, Future}

final class UserRegistrationHandler(
  usernameService: UsernameService,
  userRepository: UserRepository,
  uuidWrapper: UuidWrapper
)(implicit ec: ExecutionContext)
  extends Handler[CreateUser, Either[UserRegistrationError, UserCreated]] {

  def handle(createUser: CreateUser): Future[Either[UserRegistrationError, UserCreated]] = {
    for {
      exists <- usernameService.usernameExists(createUser.username)
      result <- if (exists) {
        Future.successful(Left(UsernameAlreadyExists))
      } else {
        val user = User.create(uuidWrapper.generateUuid(), createUser)
        userRepository.save(user).map(_ =>
          Right(UserCreated(id = user.userId, username = createUser.username, about = createUser.about))
        )
      }
    } yield result
  }
}

final case class UserCreated(id: String, username: String, about: String)
class UsernameAlreadyInUseError extends RuntimeException
