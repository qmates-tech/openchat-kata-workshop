package openchat.domain.aggregates

import openchat.domain.commands.CreateUser
import openchat.domain.entities.Post

final case class User(
  userId: String,
  username: String,
  about: String,
  password: String,
  posts: Vector[Post] = Vector.empty
) {
  def login(password: String): Boolean = this.password == password

  def addPost(post: Post): User = copy(posts = posts :+ post)
}

object User {
  def create(userId: String, createUser: CreateUser): User =
    User(
      userId = userId,
      username = createUser.username,
      about = createUser.about,
      password = createUser.password,
      posts = Vector.empty
    )
}
