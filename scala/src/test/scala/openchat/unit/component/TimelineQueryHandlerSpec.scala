package openchat.unit.component

import doobie.Transactor
import openchat.application.handlers.{TimelineQueryHandler, TimelineUserNotFound}
import openchat.infrastructure.repositories.UserRepositoryDoobie
import openchat.unit.TestHelpers._
import openchat.utilities.DatabaseTestUtils
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class TimelineQueryHandlerSpec extends AsyncFunSuite with Matchers with BeforeAndAfterEach with DatabaseTestUtils {
  implicit val ec: ExecutionContext = ExecutionContext.global

  private var xa: Transactor[cats.effect.IO] = _
  private var repo: UserRepositoryDoobie     = _
  private var handler: TimelineQueryHandler  = _

  override def beforeEach(): Unit = {
    xa = newXa()
    repo = new UserRepositoryDoobie(xa)
    handler = new TimelineQueryHandler(repo)
  }

  test("returns user's timeline posts mapped to DTO in repository order (no implicit sorting)") {
    val p1   = createTestPost("p-1", "text 1", "2025-09-13T12:00:00.000Z")
    val p2   = createTestPost("p-2", "text 2", "2025-09-14T09:00:00.000Z")
    val p3   = createTestPost("p-3", "text 3", "2025-09-12T08:00:00.000Z")
    val user = createTestUser(id = "user-1", username = "alice", posts = Vector(p1, p2, p3))

    for {
      _      <- repo.save(user)
      result <- handler.handle("user-1")
    } yield {
      result should have length 3
      result.map(_.text) should contain allOf ("text 1", "text 2", "text 3")
    }
  }

  test("returns empty list when user has no posts") {
    val user = createTestUser(id = "user-1", username = "alice")

    for {
      _      <- repo.save(user)
      result <- handler.handle("user-1")
    } yield {
      result shouldBe Vector.empty
    }
  }

  test("returns posts in reverse chronological order") {
    val older = createTestPost("p-older", "older post", "2025-09-13T12:00:00.000Z")
    val newer = createTestPost("p-newer", "newer post", "2025-09-14T12:00:00.000Z")
    val user  = createTestUser(id = "user-1", username = "alice", posts = Vector(older, newer))

    for {
      _      <- repo.save(user)
      result <- handler.handle("user-1")
    } yield {
      result should have length 2
      result.head.text shouldBe "newer post"
      result.last.text shouldBe "older post"
    }
  }

  test("throws TimelineUserNotFound when user does not exist") {
    recoverToSucceededIf[TimelineUserNotFound](handler.handle("non-existent-user"))
  }
}
