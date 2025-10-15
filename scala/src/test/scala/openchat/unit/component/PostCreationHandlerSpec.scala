package openchat.unit.component

import doobie.Transactor
import openchat.application.handlers.PostCreationHandler
import openchat.application.wrapper.UuidWrapper.UUID_PATTERN
import openchat.domain.commands.CreatePost
import openchat.domain.errors.UserNotFound
import openchat.infrastructure.repositories.UserRepositoryDoobie
import openchat.infrastructure.wrapper.UuidWrapperNative
import openchat.unit.TestHelpers._
import openchat.utilities.DatabaseTestUtils
import openchat.utilities.Utils.DATE_TIME_PATTERN
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.ExecutionContext

class PostCreationHandlerSpec extends AsyncFunSuite with Matchers with BeforeAndAfterEach with DatabaseTestUtils {
  implicit val ec: ExecutionContext = ExecutionContext.global

  private val FIXED_DATE_ISO = "2025-09-15T10:20:30.000Z"

  private var xa: Transactor[cats.effect.IO] = _
  private var repo: UserRepositoryDoobie     = _
  private val uuidWrapper                    = new UuidWrapperNative()
  private var handler: PostCreationHandler   = _

  override def beforeEach(): Unit = {
    xa = newXa()
    repo = new UserRepositoryDoobie(xa)
    handler = new PostCreationHandler(repo, uuidWrapper, Clock.systemUTC())
  }

  test("when creating a new post should return the created post data") {
    val user = createTestUser()

    for {
      _      <- repo.save(user)
      result <- handler.handle(CreatePost(userId = user.userId, text = "a text"))
    } yield {
      val created = assertRight(result)
      created.postId should fullyMatch regex UUID_PATTERN
      created.userId shouldBe user.userId
      created.text shouldBe "a text"
      created.dateTime should fullyMatch regex DATE_TIME_PATTERN
    }
  }

  test("when creating a new post should save post data correctly in repository") {
    val user = createTestUser()

    for {
      _          <- repo.save(user)
      _          <- handler.handle(CreatePost(userId = user.userId, text = "a text"))
      storedUser <- repo.get(user.userId)
    } yield {
      storedUser shouldBe defined
      val posts = storedUser.get.posts
      posts should have length 1
      val post = posts.head
      post.postId should fullyMatch regex UUID_PATTERN
      post.text shouldBe "a text"
      post.dateTime should fullyMatch regex DATE_TIME_PATTERN
    }
  }

  test("when creating a new post appends to existing posts without losing any") {
    val user = createTestUser()

    for {
      _      <- repo.save(user)
      _      <- handler.handle(CreatePost(userId = user.userId, text = "first"))
      _      <- handler.handle(CreatePost(userId = user.userId, text = "second"))
      stored <- repo.get(user.userId)
    } yield {
      stored shouldBe defined
      val posts = stored.get.posts
      posts should have length 2
      val texts = posts.map(_.text)
      texts should contain allOf ("first", "second")
    }
  }

  test("when creating a new post uses fake clock for deterministic dateTime") {
    val handlerWithFixedClock = new PostCreationHandler(repo, uuidWrapper, fixedClock(FIXED_DATE_ISO))
    val user                  = createTestUser()

    for {
      _      <- repo.save(user)
      result <- handlerWithFixedClock.handle(CreatePost(userId = user.userId, text = "time test"))
      stored <- repo.get(user.userId)
    } yield {
      val created = assertRight(result)
      created.dateTime shouldBe FIXED_DATE_ISO

      stored shouldBe defined
      val posts = stored.get.posts
      posts should have length 1
      posts.head.dateTime shouldBe FIXED_DATE_ISO
    }
  }

  test("when user does not exist should throw UserNotFound") {
    handler.handle(CreatePost(userId = randomId(), text = "missing")).map { result =>
      assertLeft(result) shouldBe UserNotFound
    }
  }
}
