package openchat.unit.component

import doobie.Transactor
import openchat.application.handlers.UserRegistrationHandler
import openchat.application.wrapper.UuidWrapper
import openchat.domain.aggregates.User
import openchat.domain.commands.CreateUser
import openchat.domain.errors.UsernameAlreadyExists
import openchat.domain.services.UsernameService
import openchat.infrastructure.repositories.UserRepositoryDoobie
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
    val uuid  = new FixedUuidWrapper("00000000-0000-4000-8000-000000000000")

    val handler = new UserRegistrationHandler(uname, repo, uuid)
    val cmd     = CreateUser(username = "alice", about = "about", password = "pwd")

    for {
      result <- handler.handle(cmd)
      savedUser <- repo.getByUsername("alice")
    } yield {
      result.isRight shouldBe true
      val created = result.getOrElse(fail("Expected Right but got Left"))
      created.id shouldBe "00000000-0000-4000-8000-000000000000"
      created.username shouldBe "alice"
      created.about shouldBe "about"
      savedUser.isDefined shouldBe true
      savedUser.get.username shouldBe "alice"
    }
  }

  test("fails with UsernameAlreadyExists when username exists") {
    val uuid  = new FixedUuidWrapper("id")
    
    // seed existing username
    val existingUser = User.create("existing-id", CreateUser("bob", "existing about", "existing pwd"))

    val handler = new UserRegistrationHandler(uname, repo, uuid)
    val cmd     = CreateUser(username = "bob", about = "x", password = "p")

    for {
      _ <- repo.save(existingUser)
      result <- handler.handle(cmd)
    } yield {
      result.isLeft shouldBe true
      result.left.getOrElse(fail("Expected Left but got Right")) shouldBe UsernameAlreadyExists
    }
  }

  test("should save user data correctly in repository") {
    val uuid  = new FixedUuidWrapper("00000000-0000-4000-8000-000000000000")

    val handler = new UserRegistrationHandler(uname, repo, uuid)
    val cmd     = CreateUser(username = "alice", about = "about", password = "secret")

    for {
      result <- handler.handle(cmd)
      storedUser <- repo.get("00000000-0000-4000-8000-000000000000")
    } yield {
      result.isRight shouldBe true
      storedUser.isDefined shouldBe true
      val user = storedUser.get
      user.userId shouldBe "00000000-0000-4000-8000-000000000000"
      user.username shouldBe "alice"
      user.about shouldBe "about"
      user.password shouldBe "secret"
    }
  }

  test("does not leak password in response") {
    val uuid  = new FixedUuidWrapper("00000000-0000-4000-8000-000000000000")

    val handler = new UserRegistrationHandler(uname, repo, uuid)
    val cmd     = CreateUser(username = "bob", about = "  ", password = "secret")

    handler.handle(cmd).map { result =>
      result.isRight shouldBe true
      val created = result.getOrElse(fail("Expected Right but got Left"))
      created.id shouldBe "00000000-0000-4000-8000-000000000000"
      created.username shouldBe "bob"
      created.about shouldBe "  "
      // Check that password is not in the response
      val resultFields = created.productElementNames.toSet
      resultFields should not contain "password"
    }
  }

  private class FixedUuidWrapper(id: String) extends UuidWrapper { override def generateUuid(): String = id }
}
