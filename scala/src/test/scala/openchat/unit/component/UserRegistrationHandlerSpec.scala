package openchat.unit.component

import doobie.Transactor
import openchat.application.handlers.UserRegistrationHandler
import openchat.application.wrapper.UuidWrapper
import openchat.domain.aggregates.User
import openchat.domain.commands.CreateUser
import openchat.domain.errors.UsernameAlreadyExists
import openchat.domain.services.UsernameService
import openchat.infrastructure.repositories.UserRepositoryDoobie
import openchat.unit.TestHelpers._
import openchat.utilities.DatabaseTestUtils
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class UserRegistrationHandlerSpec extends AsyncFunSuite with Matchers with BeforeAndAfterEach with DatabaseTestUtils {
  implicit val ec: ExecutionContext = ExecutionContext.global

  private var xa: Transactor[cats.effect.IO] = _
  private var repo: UserRepositoryDoobie     = _
  private var uname: UsernameService         = _

  override def beforeEach(): Unit = {
    xa = newXa()
    repo = new UserRepositoryDoobie(xa)
    uname = new UsernameService(repo)
  }

  test("registers user when username not exists and returns created DTO") {
    val fixedId = "00000000-0000-4000-8000-000000000000"
    val handler = new UserRegistrationHandler(uname, repo, new FixedUuidWrapper(fixedId))
    val cmd     = CreateUser(username = "alice", about = "about", password = "pwd")

    for {
      result    <- handler.handle(cmd)
      savedUser <- repo.getByUsername("alice")
    } yield {
      val created = assertRight(result)
      created.id shouldBe fixedId
      created.username shouldBe "alice"
      created.about shouldBe "about"
      savedUser shouldBe defined
      savedUser.get.username shouldBe "alice"
    }
  }

  test("fails with UsernameAlreadyExists when username exists") {
    val existingUser = createTestUser(id = "existing-id", username = "bob")
    val handler      = new UserRegistrationHandler(uname, repo, new FixedUuidWrapper("id"))
    val cmd          = CreateUser(username = "bob", about = "x", password = "p")

    for {
      _      <- repo.save(existingUser)
      result <- handler.handle(cmd)
    } yield {
      assertLeft(result) shouldBe UsernameAlreadyExists
    }
  }

  test("should save user data correctly in repository") {
    val fixedId = "00000000-0000-4000-8000-000000000000"
    val handler = new UserRegistrationHandler(uname, repo, new FixedUuidWrapper(fixedId))
    val cmd     = CreateUser(username = "alice", about = "about", password = "secret")

    for {
      result     <- handler.handle(cmd)
      storedUser <- repo.get(fixedId)
    } yield {
      assertRight(result)
      storedUser shouldBe defined
      val user = storedUser.get
      user.userId shouldBe fixedId
      user.username shouldBe "alice"
      user.about shouldBe "about"
      user.password shouldBe "secret"
    }
  }

  test("does not leak password in response") {
    val fixedId = "00000000-0000-4000-8000-000000000000"
    val handler = new UserRegistrationHandler(uname, repo, new FixedUuidWrapper(fixedId))
    val cmd     = CreateUser(username = "bob", about = "  ", password = "secret")

    handler.handle(cmd).map { result =>
      val created = assertRight(result)
      created.id shouldBe fixedId
      created.username shouldBe "bob"
      created.about shouldBe "  "
      created.productElementNames.toSet should not contain "password"
    }
  }
}
