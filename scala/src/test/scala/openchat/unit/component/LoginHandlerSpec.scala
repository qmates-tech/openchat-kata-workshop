package openchat.unit.component

import doobie.Transactor
import openchat.application.handlers.{LoginHandler, LoginUser}
import openchat.infrastructure.repositories.UserRepositoryDoobie
import openchat.unit.TestHelpers._
import openchat.utilities.DatabaseTestUtils
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class LoginHandlerSpec extends AsyncFunSuite with Matchers with BeforeAndAfterEach with DatabaseTestUtils {
  implicit val ec: ExecutionContext = ExecutionContext.global

  private var xa: Transactor[cats.effect.IO] = _
  private var repo: UserRepositoryDoobie     = _
  private var handler: LoginHandler          = _

  override def beforeEach(): Unit = {
    xa = newXa()
    repo = new UserRepositoryDoobie(xa)
    handler = new LoginHandler(repo)
  }

  test("returns Some(UserLoggedIn) on valid username/password") {
    val user = createTestUser(id = "uid-1", username = "alice", password = "secret", about = "about")

    for {
      _   <- repo.save(user)
      res <- handler.handle(LoginUser("alice", "secret"))
    } yield {
      res shouldBe defined
      val u = res.get
      u.id shouldBe "uid-1"
      u.username shouldBe "alice"
      u.about shouldBe "about"
      u.productElementNames.toSet should not contain "password"
    }
  }

  test("returns None when user does not exist") {
    handler.handle(LoginUser("ghost", "pwd")).map(_ shouldBe None)
  }

  test("returns None when password is wrong") {
    val user = createTestUser(id = "uid-2", username = "bob", password = "right", about = "")

    for {
      _   <- repo.save(user)
      res <- handler.handle(LoginUser("bob", "wrong"))
    } yield {
      res shouldBe None
    }
  }
}
