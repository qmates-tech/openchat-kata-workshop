package openchat.domain.services

import openchat.application.repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}

final class UsernameService(userRepository: UserRepository)(implicit ec: ExecutionContext) {
  def usernameExists(username: String): Future[Boolean] = userRepository.getByUsername(username).map(_.isDefined)
}
