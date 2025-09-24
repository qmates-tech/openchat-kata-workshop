package openchat.integration.api

import openchat.application.handlers.UserLoggedIn
import openchat.application.wrapper.UuidWrapper
import openchat.infrastructure.api.JsonProtocols
import openchat.utilities.HttpTestUtils._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client4._

class LoginSpec extends AnyFunSuite with BeforeAndAfterAll with JsonProtocols with Matchers {
  protected var randomUsername: String = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    randomUsername = s"user-${generateRandomSuffix}"
    basicRequest
      .post(uri"$BASE_URI/users")
      .contentType("application/json")
      .body(s"""{"username":"$randomUsername","password":"valid_password","about":"Test user"}""")
      .send(backend)
  }

  test("POST /login should return 200 status code on successful login") {
    val loginResponse = basicRequest
      .post(uri"$BASE_URI/login")
      .contentType("application/json")
      .body(s"""{"username":"$randomUsername","password":"valid_password"}""")
      .send(backend)

    loginResponse.code.code shouldBe 200
  }

  test("POST /login should return response with application/json content type") {
    val loginResponse = basicRequest
      .post(uri"$BASE_URI/login")
      .contentType("application/json")
      .body(s"""{"username":"$randomUsername","password":"valid_password"}""")
      .send(backend)

    loginResponse.contentType should contain("application/json")
  }

  test("POST /login should return user data without password in response body") {
    val loginResponse = basicRequest
      .post(uri"$BASE_URI/login")
      .contentType("application/json")
      .body(s"""{"username":"$randomUsername","password":"valid_password"}""")
      .send(backend)

    val loginResp = parseJson(loginResponse).convertTo[UserLoggedIn]
    loginResp.id should fullyMatch regex UuidWrapper.UUID_PATTERN
    val jsonObj = parseJson(loginResponse).asJsObject
    jsonObj.fields should not contain key("password")
  }

  test("POST /login should return 401 status code on invalid credentials") {
    val failedLoginResponse = basicRequest
      .post(uri"$BASE_URI/login")
      .contentType("application/json")
      .body(s"""{"username":"$randomUsername","password":"wrong_password"}""")
      .send(backend)

    failedLoginResponse.code.code shouldBe 401
  }

}
