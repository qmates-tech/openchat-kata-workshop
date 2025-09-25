package openchat.unit.component

import doobie.Transactor
import openchat.application.handlers.PostCreationHandler
import openchat.application.wrapper.UuidWrapper.UUID_PATTERN
import openchat.domain.commands.CreatePost
import openchat.domain.errors.UserNotFound
import openchat.infrastructure.repositories.UserRepositoryDoobie
import openchat.infrastructure.wrapper.UuidWrapperNative
import openchat.utilities.DatabaseTestUtils
import openchat.utilities.Utils.{DATE_TIME_PATTERN, createUser}
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
    val alreadyExistingUser = createUser()

    for {
      _      <- repo.save(alreadyExistingUser)
      result <- handler.handle(CreatePost(userId = alreadyExistingUser.userId, text = "a text"))
    } yield {
      result.isRight shouldBe true
      val created = result.getOrElse(fail("Expected Right but got Left"))
      created.postId should fullyMatch regex UUID_PATTERN
      created.userId shouldBe alreadyExistingUser.userId
      created.text shouldBe "a text"
      created.dateTime should fullyMatch regex DATE_TIME_PATTERN
    }
  }

  test("when creating a new post should save post data correctly in repository") {
    val alreadyExistingUser = createUser()

    for {
      _          <- repo.save(alreadyExistingUser)
      _          <- handler.handle(CreatePost(userId = alreadyExistingUser.userId, text = "a text"))
      storedUser <- repo.get(alreadyExistingUser.userId)
    } yield {
      storedUser.isDefined shouldBe true
      val posts = storedUser.get.posts
      posts should have length 1
      val post = posts.head
      post.postId should fullyMatch regex UUID_PATTERN
      post.text shouldBe "a text"
      post.dateTime should fullyMatch regex DATE_TIME_PATTERN
    }
  }

  test("when creating a new post appends to existing posts without losing any") {
    val user = createUser()

    for {
      _      <- repo.save(user)
      _      <- handler.handle(CreatePost(userId = user.userId, text = "first"))
      _      <- handler.handle(CreatePost(userId = user.userId, text = "second"))
      stored <- repo.get(user.userId)
    } yield {
      stored.isDefined shouldBe true
      val posts = stored.get.posts
      posts should have length 2
      val texts = posts.map(_.text)
      texts should contain allOf ("first", "second")
    }
  }

  test("when creating a new post uses fake clock for deterministic dateTime") {
    val fixedClock            = Clock.fixed(Instant.parse(FIXED_DATE_ISO), ZoneOffset.UTC)
    val handlerWithFixedClock = new PostCreationHandler(repo, uuidWrapper, fixedClock)
    val user                  = createUser()

    for {
      _      <- repo.save(user)
      result <- handlerWithFixedClock.handle(CreatePost(userId = user.userId, text = "time test"))
      stored <- repo.get(user.userId)
    } yield {
      result.isRight shouldBe true
      val created = result.getOrElse(fail("Expected Right but got Left"))
      created.dateTime shouldBe FIXED_DATE_ISO

      stored.isDefined shouldBe true
      val posts = stored.get.posts
      posts should have length 1
      posts.head.dateTime shouldBe FIXED_DATE_ISO
    }
  }

  test("when user does not exist should throw UserNotFound") {
    handler.handle(CreatePost(userId = uuidWrapper.generateUuid(), text = "missing")).map { result =>
      result.isLeft shouldBe true
      result.left.getOrElse(fail("Expected Left but got Right")) shouldBe UserNotFound
    }
  }
}
