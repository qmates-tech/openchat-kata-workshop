package openchat.application.handlers

import openchat.application.Handler
import openchat.application.repositories.UserRepository
import openchat.application.wrapper.UuidWrapper
import openchat.domain.commands.CreatePost
import openchat.domain.entities.Post
import openchat.domain.errors.{InvalidPost, PostCreationError, UserNotFound}

import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}

final case class PostCreated(postId: String, userId: String, text: String, dateTime: String)

final class PostCreationHandler(userRepository: UserRepository, uuidWrapper: UuidWrapper, clock: Clock)(implicit
  ec: ExecutionContext
) extends Handler[CreatePost, Either[PostCreationError, PostCreated]] {
  def handle(cmd: CreatePost): Future[Either[PostCreationError, PostCreated]] = {
    for {
      maybeUser <- userRepository.get(cmd.userId)
      result <- maybeUser match {
        case None => Future.successful(Left(UserNotFound))
        case Some(user) =>
          Post.create(uuidWrapper.generateUuid(), cmd, clock) match {
            case Left(_) => Future.successful(Left(InvalidPost))
            case Right(post) =>
              userRepository.save(user.addPost(post)).map(_ =>
                Right(PostCreated(postId = post.postId, userId = cmd.userId, text = post.text, dateTime = post.dateTime))
              )
          }
      }
    } yield result
  }
}
