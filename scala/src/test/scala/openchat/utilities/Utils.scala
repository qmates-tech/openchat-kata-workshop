package openchat.utilities

import openchat.domain.aggregates.User
import openchat.domain.commands.CreateUser
import openchat.domain.entities.Post

import scala.util.matching.Regex

object Utils {
  // Common regex patterns
  val DATE_TIME_PATTERN: Regex = """^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z$""".r
  val UUID_PATTERN: Regex      = """^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$""".r

  def createUser(posts: Vector[Post] = Vector.empty): User =
    User
      .create(
        userId = "1-2-3-4-5",
        createUser = CreateUser(username = "username", password = "valid password", about = "about")
      )
      .copy(posts = posts)
}
