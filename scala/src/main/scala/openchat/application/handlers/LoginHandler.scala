package openchat.application.handlers

import openchat.application.Handler
import openchat.application.repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}

final case class LoginUser(username: String, password: String)
final case class UserLoggedIn(id: String, username: String, about: String)

final class LoginHandler(userRepository: UserRepository)(implicit ec: ExecutionContext)
    extends Handler[LoginUser, Option[UserLoggedIn]] {
  def handle(loginUser: LoginUser): Future[Option[UserLoggedIn]] = {
    userRepository.getByUsername(loginUser.username).map {
      case None       => None
      case Some(user) =>
        if (user.login(loginUser.password))
          Some(UserLoggedIn(id = user.userId, username = user.username, about = user.about))
        else None
    }
  }
}
