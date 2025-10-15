package openchat.unit.domain.services

import openchat.application.repositories.UserRepository
import openchat.domain.aggregates.User
import openchat.domain.commands.CreateUser
import openchat.domain.services.UsernameService
import openchat.utilities.Utils.createUser
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class UsernameServiceSpec extends AsyncFunSuite with Matchers {

  test("should return false when username does not exist") {
    val userRepository  = new UserRepositoryStubGetByUsernameNull()
    val usernameService = new UsernameService(userRepository)

    usernameService.usernameExists("Alice").map { usernameExists =>
      usernameExists shouldBe false
    }
  }

  test("should return true when username exists") {
    val userRepository  = new UserRepositoryStubGetByUsernameSomething()
    val usernameService = new UsernameService(userRepository)

    usernameService.usernameExists("Alice").map { usernameExists =>
      usernameExists shouldBe true
    }
  }

  test("should propagate repository errors") {
    val userRepository  = new UserRepositoryStubThrows()
    val usernameService = new UsernameService(userRepository)

    recoverToSucceededIf[RuntimeException] {
      usernameService.usernameExists("Bob")
    }
  }

  // Test doubles
  class UserRepositoryStubGetByUsernameNull extends UserRepository {
    override def getByUsername(username: String): Future[Option[User]] = Future.successful(None)
    override def save(user: User): Future[Unit]                        = Future.successful(())
    override def get(userId: String): Future[Option[User]]             = Future.successful(None)
  }

  class UserRepositoryStubGetByUsernameSomething extends UserRepository {
    override def getByUsername(username: String): Future[Option[User]] = {
      val user = User.create("1-2-3-4-5", CreateUser("username", "password", "about"))
      Future.successful(Some(user))
    }
    override def save(user: User): Future[Unit]            = Future.successful(())
    override def get(userId: String): Future[Option[User]] = Future.successful(None)
  }

  class UserRepositoryStubThrows extends UserRepository {
    override def getByUsername(username: String): Future[Option[User]] = Future.failed(new RuntimeException("boom"))
    override def save(user: User): Future[Unit]                        = Future.successful(())
    override def get(userId: String): Future[Option[User]]             = Future.successful(None)
  }
}
