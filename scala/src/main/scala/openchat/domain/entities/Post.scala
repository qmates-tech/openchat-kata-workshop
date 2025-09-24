package openchat.domain.entities

import openchat.domain.commands.CreatePost
import openchat.domain.entities.Post.inappropriateWords
import openchat.domain.errors.InvalidPost

import java.time.format.DateTimeFormatter
import java.time.{Clock, Instant, ZoneOffset}

final case class Post(postId: String, text: String, dateTime: String) {
  private def isValid: Boolean = {
    val lower = text.toLowerCase
    text.nonEmpty && !inappropriateWords.exists(w => lower.contains(w))
  }
}

class InvalidPostException extends RuntimeException

object Post {
  def create(postId: String, cmd: CreatePost, clock: Clock = Clock.systemUTC()): Either[InvalidPost.type, Post] = {
    val post = Post(postId, cmd.text, dateFormatter.format(Instant.now(clock)))
    if (!post.isValid) Left(InvalidPost)
    else Right(post)
  }

  private val inappropriateWords: List[String] = List("orange", "elephant", "ice cream")

  private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)
}
