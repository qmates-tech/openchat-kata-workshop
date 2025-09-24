package openchat.integration.api

import openchat.application.handlers.UserCreated
import openchat.application.wrapper.UuidWrapper
import openchat.infrastructure.api.{ErrorMessage, JsonProtocols}
import openchat.utilities.HttpTestUtils._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client4._

class UserRegistrationSpec extends AnyFunSuite with Matchers with JsonProtocols {

  test("POST /users should create a user with 201 Created") {
    val username = s"alice-${generateRandomSuffix}"

    val response = basicRequest
      .post(uri"$BASE_URI/users")
      .contentType("application/json")
      .body(s"""{"username":"$username","password":"pwd","about":"hi"}""")
      .send(backend)

    response.code.code shouldBe 201
  }

  test("POST /users should return response with application/json content type") {
    val username = s"alice-${generateRandomSuffix}"

    val response = basicRequest
      .post(uri"$BASE_URI/users")
      .contentType("application/json")
      .body(s"""{"username":"$username","password":"pwd","about":"hi"}""")
      .send(backend)

    response.contentType should contain("application/json")
  }

  test("POST /users should return user data without password in response body") {
    val username = s"alice-${generateRandomSuffix}"

    val response = basicRequest
      .post(uri"$BASE_URI/users")
      .contentType("application/json")
      .body(s"""{"username":"$username","password":"pwd","about":"hi"}""")
      .send(backend)

    val resp = parseJson(response).convertTo[UserCreated]
    resp.id should fullyMatch regex UuidWrapper.UUID_PATTERN
    resp.username shouldBe username
    val jsonObj = parseJson(response).asJsObject
    jsonObj.fields should not contain key("password")
  }

  test("POST /users should return 400 BadRequest with message when username already exists") {
    val username = s"dup-${generateRandomSuffix}"
    val firstResponse = basicRequest
      .post(uri"$BASE_URI/users")
      .contentType("application/json")
      .body(s"""{"username":"$username","password":"pwd","about":"hi"}""")
      .send(backend)
    firstResponse.code.code shouldBe 201

    val secondResponse = basicRequest
      .post(uri"$BASE_URI/users")
      .contentType("application/json")
      .body(s"""{"username":"$username","password":"pwd","about":"hi"}""")
      .send(backend)

    secondResponse.code.code shouldBe 400
    val err = parseJson(secondResponse).convertTo[ErrorMessage]
    err.message shouldBe "Username already in use"
  }
}
