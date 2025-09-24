package openchat.application.handlers

import openchat.application.Handler
import openchat.application.repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}

final case class TimelinePostView(postId: String, userId: String, text: String, dateTime: String)
final class TimelineUserNotFound extends RuntimeException

final class TimelineQueryHandler(userRepository: UserRepository)(implicit ec: ExecutionContext)
    extends Handler[String, Vector[TimelinePostView]] {
  def handle(userId: String): Future[Vector[TimelinePostView]] = {
    for {
      maybeUser <- userRepository.get(userId)
      user      <- maybeUser match {
        case None        => Future.failed(new TimelineUserNotFound)
        case Some(user0) => Future.successful(user0)
      }
    } yield user.posts.map(p =>
      TimelinePostView(postId = p.postId, userId = userId, text = p.text, dateTime = p.dateTime)
    )
  }
}
