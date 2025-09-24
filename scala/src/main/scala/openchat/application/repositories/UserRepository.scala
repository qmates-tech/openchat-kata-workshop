package openchat.application.repositories

import openchat.domain.aggregates.User

import scala.concurrent.Future

trait UserRepository {
  def save(user: User): Future[Unit]
  def get(userId: String): Future[Option[User]]
  def getByUsername(username: String): Future[Option[User]]
}
