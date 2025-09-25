package openchat.unit.component

import doobie.Transactor
import openchat.application.handlers.{LoginHandler, LoginUser}
import openchat.domain.aggregates.User
import openchat.domain.commands.CreateUser
import openchat.infrastructure.repositories.UserRepositoryDoobie
import openchat.utilities.DatabaseTestUtils
import openchat.utilities.Utils.createUser
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
    val user = User.create("uid-1", CreateUser("alice", "about", "secret"))

    for {
      _   <- repo.save(user)
      res <- handler.handle(LoginUser("alice", "secret"))
    } yield {
      res.isDefined shouldBe true
      val u = res.get
      u.id shouldBe "uid-1"
      u.username shouldBe "alice"
      u.about shouldBe "about"
      // Check that password is not leaked in the response
      val resultFields = u.productElementNames.toSet
      resultFields should not contain "password"
    }
  }

  test("returns None when user does not exist") {
    handler.handle(LoginUser("ghost", "pwd")).map(_ shouldBe None)
  }

  test("returns None when password is wrong") {
    val user = User.create("uid-2", CreateUser("bob", "", "right"))

    for {
      _   <- repo.save(user)
      res <- handler.handle(LoginUser("bob", "wrong"))
    } yield {
      res shouldBe None
    }
  }
}
