package openchat.integration.api

import openchat.application.handlers._
import openchat.infrastructure.api.JsonProtocols
import openchat.utilities.HttpTestUtils._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client4._

class TimelineSpec extends AnyFunSuite with BeforeAndAfterAll with JsonProtocols with Matchers {
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

  test("GET /users/{id}/timeline should return 200 and posts as array when fetching timeline") {
    val timelineResponse = basicRequest
      .get(uri"$BASE_URI/users/$currentUserId/timeline")
      .send(backend)

    timelineResponse.code.code shouldBe 200
  }

  test("GET /users/{id}/timeline should return response with application/json content type") {
    val timelineResponse = basicRequest
      .get(uri"$BASE_URI/users/$currentUserId/timeline")
      .send(backend)

    timelineResponse.contentType should contain("application/json")
  }

  test("GET /users/{id}/timeline should return empty array when user has no posts") {
    val timelineResponse = basicRequest
      .get(uri"$BASE_URI/users/$currentUserId/timeline")
      .send(backend)

    val posts = parseJsonArray(timelineResponse)
    posts shouldBe empty
  }

  test("GET /users/{id}/timeline should return posts when exists") {
    basicRequest
      .post(uri"$BASE_URI/users/$currentUserId/timeline")
      .contentType("application/json")
      .body("""{"text":"hello from api"}""")
      .send(backend)

    val timelineResponse = basicRequest
      .get(uri"$BASE_URI/users/$currentUserId/timeline")
      .send(backend)

    val posts = parseJsonArray(timelineResponse).map(_.convertTo[TimelinePostView])
    posts should have size 1
    posts.map(_.userId).distinct shouldBe Vector(currentUserId)
    posts.map(_.text) shouldBe Vector("hello from api")
  }

  test("GET /users/{id}/timeline should return 404 status code with invalid userId") {
    val invalidResponse = basicRequest
      .get(uri"$BASE_URI/users/invalid-uuid/timeline")
      .send(backend)

    invalidResponse.code.code shouldBe 404
  }

  test("GET /users/{id}/timeline should return 404 status code when user does not exist") {
    val notFoundUser = java.util.UUID.randomUUID().toString

    val notFoundResponse = basicRequest
      .get(uri"$BASE_URI/users/$notFoundUser/timeline")
      .send(backend)

    notFoundResponse.code.code shouldBe 404
  }

}
