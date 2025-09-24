package openchat.domain.errors

sealed trait UserRegistrationError
case object UsernameAlreadyExists           extends UserRegistrationError
case class ValidationError(message: String) extends UserRegistrationError

sealed trait PostCreationError
case object UserNotFound extends PostCreationError
case object InvalidPost  extends PostCreationError
