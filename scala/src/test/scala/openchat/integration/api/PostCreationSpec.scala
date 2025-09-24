package openchat.integration.api

import openchat.application.handlers._
import openchat.application.wrapper.UuidWrapper
import openchat.infrastructure.api.JsonProtocols
import openchat.utilities.HttpTestUtils._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client4._

class PostCreationSpec extends AnyFunSuite with BeforeAndAfterAll with JsonProtocols with Matchers {
  protected var currentUserId: String = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    val randomUsername = s"user-${generateRandomSuffix}"
    val createResponse = basicRequest
      .post(uri"$BASE_URI/users")
      .contentType("application/json")
      .body(s"""{"username":"$randomUsername","password":"valid_password","about":"Test user"}""")
      .send(backend)
    currentUserId = extractJsonField(createResponse, "id")
  }

  test("POST /users/{id}/timeline should create a post with 201 Created") {
    val postResponse = basicRequest
      .post(uri"$BASE_URI/users/$currentUserId/timeline")
      .contentType("application/json")
      .body("""{"text":"hello world"}""")
      .send(backend)

    postResponse.code.code shouldBe 201
  }

  test("POST /users/{id}/timeline should return response with application/json content type") {
    val postResponse = basicRequest
      .post(uri"$BASE_URI/users/$currentUserId/timeline")
      .contentType("application/json")
      .body("""{"text":"hello world"}""")
      .send(backend)

    postResponse.contentType should contain("application/json")
  }

  test("POST /users/{id}/timeline should return the created post in response body") {
    val postResponse = basicRequest
      .post(uri"$BASE_URI/users/$currentUserId/timeline")
      .contentType("application/json")
      .body("""{"text":"hello world"}""")
      .send(backend)

    val post = parseJson(postResponse).convertTo[PostCreated]
    post.postId should fullyMatch regex UuidWrapper.UUID_PATTERN
    val jsonObj = parseJson(postResponse).asJsObject
    jsonObj.fields should contain key "text"
  }

  test("POST /users/{id}/timeline should return 404 status code with invalid userId") {
    val invalidPostResponse = basicRequest
      .post(uri"$BASE_URI/users/invalid-uuid/timeline")
      .contentType("application/json")
      .body("""{"text":"hello world"}""")
      .send(backend)

    invalidPostResponse.code.code shouldBe 404
  }

  test("POST /users/{id}/timeline should return 404 status code when user does not exist") {
    val notFoundUser = java.util.UUID.randomUUID().toString

    val notFoundResponse = basicRequest
      .post(uri"$BASE_URI/users/$notFoundUser/timeline")
      .contentType("application/json")
      .body("""{"text":"hello world"}""")
      .send(backend)

    notFoundResponse.code.code shouldBe 404
  }

  test("POST /users/{id}/timeline should return 400 status code when text is missing") {
    val missingTextResponse = basicRequest
      .post(uri"$BASE_URI/users/$currentUserId/timeline")
      .contentType("application/json")
      .body("{}")
      .send(backend)

    missingTextResponse.code.code shouldBe 400
  }

  test("POST /users/{id}/timeline should return 400 status code when text is empty") {
    val emptyTextResponse = basicRequest
      .post(uri"$BASE_URI/users/$currentUserId/timeline")
      .contentType("application/json")
      .body("""{"text":"   "}""")
      .send(backend)

    emptyTextResponse.code.code shouldBe 400
  }

  test("POST /users/{id}/timeline should return 400 status code when text contains inappropriate language") {
    val badLanguageResponse = basicRequest
      .post(uri"$BASE_URI/users/$currentUserId/timeline")
      .contentType("application/json")
      .body("""{"text":"the orange word is bad!"}""")
      .send(backend)

    badLanguageResponse.code.code shouldBe 400
  }
}
