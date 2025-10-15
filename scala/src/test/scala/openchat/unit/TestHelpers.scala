package openchat.unit

import openchat.application.wrapper.UuidWrapper
import openchat.domain.aggregates.User
import openchat.domain.commands.CreateUser
import openchat.domain.entities.Post
import openchat.infrastructure.wrapper.UuidWrapperNative
import org.scalatest.Assertions.fail

import java.time.{Clock, Instant, ZoneOffset}
import scala.util.Random

/** Common test utilities and helpers for unit tests
  */
object TestHelpers {

  private val uuidGen = new UuidWrapperNative()

  def randomId(): String = uuidGen.generateUuid()

  def randomUsername(): String = s"user${Random.nextInt(10000)}"

  def createTestUser(
    id: String = randomId(),
    username: String = randomUsername(),
    password: String = "password123",
    about: String = "Test user",
    posts: Vector[Post] = Vector.empty
  ): User = {
    val user = User.create(id, CreateUser(username, about, password))
    posts.foldLeft(user)(_ addPost _)
  }

  def createTestPost(
    id: String = randomId(),
    text: String = "Test post",
    dateTime: String = "2025-01-01T12:00:00.000Z"
  ): Post = Post(id, text, dateTime)

  def fixedClock(instant: String = "2025-09-15T10:20:30.000Z"): Clock =
    Clock.fixed(Instant.parse(instant), ZoneOffset.UTC)

  // Common assertions
  def assertRight[L, R](either: Either[L, R]): R = {
    either.fold(
      left => fail(s"Expected Right but got Left: $left"),
      identity
    )
  }

  def assertLeft[L, R](either: Either[L, R]): L = {
    either.fold(
      identity,
      right => fail(s"Expected Left but got Right: $right")
    )
  }

  class FixedUuidWrapper(id: String) extends UuidWrapper {
    override def generateUuid(): String = id
  }
}
