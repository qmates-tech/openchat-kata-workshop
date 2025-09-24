package openchat.domain.commands

final case class CreateUser(username: String, about: String, password: String)
