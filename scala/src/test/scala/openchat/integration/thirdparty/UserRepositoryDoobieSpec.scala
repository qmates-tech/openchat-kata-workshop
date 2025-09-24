package openchat.integration.thirdparty

import doobie.Transactor
import openchat.domain.aggregates.User
import openchat.domain.entities.Post
import openchat.infrastructure.repositories.UserRepositoryDoobie
import openchat.utilities.DatabaseTestUtils
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class UserRepositoryDoobieSpec extends AnyFunSuite with Matchers with BeforeAndAfterEach with DatabaseTestUtils {
  implicit val ec: ExecutionContext = ExecutionContext.global

  private def await[A](f: scala.concurrent.Future[A], atMost: FiniteDuration = 5.seconds): A = Await.result(f, atMost)

  private var xa: Transactor[cats.effect.IO] = _
  private var repo: UserRepositoryDoobie     = _

  override def beforeEach(): Unit = {
    xa = newXa()
    repo = new UserRepositoryDoobie(xa)
  }

  test("save should persist a user and getByUsername should retrieve it without posts") {
    val u = User(userId = "1-2-3-4-5", username = "username", about = "about", password = "valid password")
    await(repo.save(u))

    val loaded = await(repo.getByUsername("username"))
    loaded.isDefined shouldBe true
    loaded.get shouldBe u.copy(posts = Vector.empty)
  }

  test("save should update an existing user attributes") {
    await(repo.save(User(userId = "1-2-3-4-5", username = "username", about = "about", password = "valid password")))

    await(
      repo.save(
        User(userId = "1-2-3-4-5", username = "username updated", about = "about updated", password = "valid password")
      )
    )

    val loaded = await(repo.get("1-2-3-4-5")).get
    loaded.username shouldBe "username updated"
    loaded.about shouldBe "about updated"
  }

  test("save should persist a user with posts") {
    val p1 = Post(postId = "p-1-2-3-4", text = "text 1", dateTime = "2025-09-13T12:00:00.000Z")
    val p2 = Post(postId = "p-5-6-7-8", text = "text 2", dateTime = "2025-09-13T12:00:00.000Z")
    val u  = User(
      userId = "1-2-3-4-5",
      username = "username",
      about = "about",
      password = "valid password",
      posts = Vector(p1, p2)
    )

    await(repo.save(u))

    val loaded = await(repo.get(u.userId)).get
    loaded.posts should have length 2
    loaded.posts(0).text shouldBe "text 1"
    loaded.posts(1).text shouldBe "text 2"
  }

  test("save should return posts ordered by dateTime DESC") {
    val p1 = Post(postId = "p-1-2-3-4", text = "older post", dateTime = "2025-09-12T12:00:00.000Z")
    val p2 = Post(postId = "p-5-6-7-8", text = "newer post", dateTime = "2025-09-14T12:00:00.000Z")
    val u  = User(
      userId = "1-2-3-4-5",
      username = "username",
      about = "about",
      password = "valid password",
      posts = Vector(p1, p2)
    )

    await(repo.save(u))

    val loaded = await(repo.get(u.userId)).get
    loaded.posts.map(_.text) shouldBe Vector("newer post", "older post")
    loaded.posts.map(_.postId) shouldBe Vector("p-5-6-7-8", "p-1-2-3-4")
  }

  test("save should preserve dateTime when retrieving posts") {
    val p1 = Post(postId = "p-1-2-3-4", text = "text 1", dateTime = "2025-09-13T12:00:00.000Z")
    val p2 = Post(postId = "p-5-6-7-8", text = "text 2", dateTime = "2025-09-13T12:00:01.000Z")
    val u  = User(
      userId = "1-2-3-4-5",
      username = "username",
      about = "about",
      password = "valid password",
      posts = Vector(p1, p2)
    )

    await(repo.save(u))

    val loaded = await(repo.get(u.userId)).get
    // dateTime should look like ISO-8601 with milliseconds Z
    val isoMillis = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$".r
    all(loaded.posts.map(_.dateTime)) should fullyMatch regex isoMillis.regex
  }

  test("save should update posts when saving an existing user with additional posts") {
    val p1 = Post(postId = "p-1-2-3-4", text = "text 1", dateTime = "2025-09-13T12:00:00.000Z")
    val p2 = Post(postId = "p-5-6-7-8", text = "text 2", dateTime = "2025-09-13T12:00:01.000Z")
    await(
      repo.save(
        User(
          userId = "1-2-3-4-5",
          username = "username",
          about = "about",
          password = "valid password",
          posts = Vector(p1, p2)
        )
      )
    )

    val p3 = Post(postId = "p-2-3-4-5", text = "text 3", dateTime = "2025-09-13T12:00:02.000Z")
    await(
      repo.save(
        User(
          userId = "1-2-3-4-5",
          username = "username",
          about = "about",
          password = "valid password",
          posts = Vector(p1, p2, p3)
        )
      )
    )

    val loaded = await(repo.get("1-2-3-4-5")).get
    loaded.posts.map(_.text) shouldBe Vector("text 3", "text 2", "text 1")
  }

  test("save should fail when username is duplicated (unique constraint)") {
    await(repo.save(User(userId = "1-2-3-4-5", username = "username", about = "about", password = "valid password")))

    assertThrows[Throwable] {
      await(repo.save(User(userId = "6-7-8-9-0", username = "username", about = "About user 2", password = "pass2")))
    }
  }

  test("get should return None when user not found") {
    await(repo.get("1-2-3-4-5")) shouldBe None
  }

  test("get should return saved user without posts when none exist") {
    await(repo.save(User(userId = "1-2-3-4-5", username = "username", about = "about", password = "valid password")))
    val user = await(repo.get("1-2-3-4-5")).get
    user.posts shouldBe Vector.empty
  }

  test("get should return user with posts when posts exist") {
    val u = User(
      userId = "1-2-3-4-5",
      username = "username",
      about = "about",
      password = "valid password",
      posts = Vector(
        Post(postId = "p-1-2-3-4", text = "text 1", dateTime = "2025-09-13T12:00:00.000Z"),
        Post(postId = "p-5-6-7-8", text = "text 2", dateTime = "2025-09-13T12:00:01.000Z")
      )
    )
    await(repo.save(u))

    val retrieved = await(repo.get("1-2-3-4-5")).get
    retrieved.posts.length shouldBe 2
    retrieved.posts(0) shouldBe Post("p-5-6-7-8", "text 2", "2025-09-13T12:00:01.000Z")
    retrieved.posts(1) shouldBe Post("p-1-2-3-4", "text 1", "2025-09-13T12:00:00.000Z")
  }

  test("getByUsername should return None when user not found") {
    await(repo.getByUsername("not_existing_user")) shouldBe None
  }

  test("getByUsername should return user without posts even if posts exist") {
    val u = User(
      userId = "1-2-3-4-5",
      username = "username",
      about = "about",
      password = "valid password",
      posts = Vector(
        Post(postId = "p-1-2-3-4", text = "text 1", dateTime = "2025-09-13T12:00:00.000Z"),
        Post(postId = "p-5-6-7-8", text = "text 2", dateTime = "2025-09-13T12:00:01.000Z")
      )
    )
    await(repo.save(u))

    val retrieved = await(repo.getByUsername("username")).get
    retrieved.posts shouldBe Vector.empty
  }

  test("save then get should return the exact saved user without posts") {
    val u = User(
      userId = "1-2-3-4-5",
      username = "username",
      about = "about",
      password = "valid password",
      posts = Vector.empty
    )
    await(repo.save(u))

    val loaded = await(repo.get("1-2-3-4-5"))
    loaded.isDefined shouldBe true
    loaded.get shouldBe u
  }

  test("save should handle updating an existing post's text and dateTime") {
    val original = Post(postId = "p-1-2-3-4", text = "original text", dateTime = "2025-09-13T12:00:00.000Z")
    await(
      repo.save(
        User(
          userId = "1-2-3-4-5",
          username = "username",
          about = "about",
          password = "valid password",
          posts = Vector(original)
        )
      )
    )

    val updated = Post(postId = "p-1-2-3-4", text = "updated text", dateTime = "2025-09-13T13:00:00.000Z")
    await(
      repo.save(
        User(
          userId = "1-2-3-4-5",
          username = "username",
          about = "about",
          password = "valid password",
          posts = Vector(updated)
        )
      )
    )

    val loaded = await(repo.get("1-2-3-4-5")).get
    loaded.posts should have length 1
    loaded.posts.head.text shouldBe "updated text"
    loaded.posts.head.dateTime shouldBe "2025-09-13T13:00:00.000Z"
  }
}
