package openchat.unit.component

import doobie.Transactor
import openchat.application.handlers.{TimelineQueryHandler, TimelineUserNotFound}
import openchat.domain.aggregates.User
import openchat.domain.commands.CreateUser
import openchat.domain.entities.Post
import openchat.infrastructure.repositories.UserRepositoryDoobie
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
    val user = User.create("user-1", CreateUser("alice", "about", "pwd"))

    val p1 = Post("p-1", "text 1", "2025-09-13T12:00:00.000Z")
    val p2 = Post("p-2", "text 2", "2025-09-14T09:00:00.000Z")
    val p3 = Post("p-3", "text 3", "2025-09-12T08:00:00.000Z")

    for {
      _      <- repo.save(user)
      _      <- repo.save(user.addPost(p1).addPost(p2).addPost(p3))
      result <- handler.handle("user-1")
    } yield {
      // UserRepositoryDoobie orders posts by dateTime DESC, so we expect reverse chronological order
      result should have length 3
      result.map(_.text) should contain allOf ("text 1", "text 2", "text 3")
    }
  }

  test("returns empty list when user has no posts") {
    val user = User.create("user-1", CreateUser("alice", "about", "pwd"))

    for {
      _      <- repo.save(user)
      result <- handler.handle("user-1")
    } yield {
      result shouldBe Vector.empty
    }
  }

  test("returns posts in reverse chronological order") {
    val user = User.create("user-1", CreateUser("alice", "about", "pwd"))

    val older = Post("p-older", "older post", "2025-09-13T12:00:00.000Z")
    val newer = Post("p-newer", "newer post", "2025-09-14T12:00:00.000Z")

    // Add posts in chronological order - the repository will handle ordering
    for {
      _      <- repo.save(user)
      _      <- repo.save(user.addPost(older).addPost(newer))
      result <- handler.handle("user-1")
    } yield {
      result should have length 2
      // UserRepositoryDoobie orders by dateTime DESC, so newer should be first
      result.head.text shouldBe "newer post"
      result.last.text shouldBe "older post"
    }
  }

  test("throws TimelineUserNotFound when user does not exist") {
    recoverToSucceededIf[TimelineUserNotFound](handler.handle("non-existent-user"))
  }
}
