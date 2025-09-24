package openchat.customer

import openchat.infrastructure.api.JsonProtocols
import openchat.utilities.HttpTestUtils._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client4._

class UserCreatesAPostSpec extends AnyFunSuite with Matchers with JsonProtocols {

  test("Customer can create a post using the real running application") {
    val username = s"Alice-$generateRandomSuffix"
    val password = "alk8325d"

    // Create user
    basicRequest
      .post(uri"$BASE_URI/users")
      .contentType("application/json")
      .body(s"""{"username":"$username","password":"$password","about":"I love playing the piano and traveling."}""")
      .send(backend)

    // login
    val loginRes = basicRequest
      .post(uri"$BASE_URI/login")
      .contentType("application/json")
      .body(s"""{"username":"$username","password":"$password"}""")
      .send(backend)

    val userId = extractJsonField(loginRes, "id")

    // Create a post
    val postRes = basicRequest
      .post(uri"$BASE_URI/users/${userId}/timeline")
      .contentType("application/json")
      .body("""{"text":"Hello, I'm Alice"}""")
      .send(backend)

    // look at the timeline
    val timelineRes = basicRequest
      .get(uri"$BASE_URI/users/${userId}/timeline")
      .send(backend)

    val posts = parseJsonArray(timelineRes)
    posts should have size 1
    posts.head shouldBe parseJson(postRes)
  }

}
