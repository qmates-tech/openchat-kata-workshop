package openchat.unit.domain.entities

import openchat.domain.commands.CreatePost
import openchat.domain.entities.Post
import openchat.unit.TestHelpers._
import openchat.utilities.Utils.{DATE_TIME_PATTERN, UUID_PATTERN}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class PostSpec extends AnyFunSuite with Matchers with TableDrivenPropertyChecks {
  private val FIXED_ISO = "2025-09-15T10:20:30.000Z"

  test("should create a valid Post") {
    val post        = Post.create(randomId(), CreatePost(userId = randomId(), text = "text"))
    val createdPost = assertRight(post)
    createdPost.text shouldBe "text"
  }

  test("should have an id in UUID format") {
    val post        = Post.create(randomId(), CreatePost(userId = randomId(), text = "text"))
    val createdPost = assertRight(post)
    createdPost.postId should fullyMatch regex UUID_PATTERN
  }

  test("should have a date-time") {
    val post        = Post.create(randomId(), CreatePost(userId = randomId(), text = "text"))
    val createdPost = assertRight(post)
    createdPost.dateTime should fullyMatch regex DATE_TIME_PATTERN
  }

  test("uses fake clock to produce deterministic ISO ending with .000Z") {
    val post = Post.create(randomId(), CreatePost(userId = randomId(), text = "deterministic"), fixedClock(FIXED_ISO))
    val createdPost = assertRight(post)
    createdPost.dateTime shouldBe FIXED_ISO
    createdPost.dateTime.endsWith(".000Z") shouldBe true
  }

  test("createFromAttributes preserves id/text/dateTime as-is") {
    val attrs   = Post(postId = "00000000-0000-4000-8000-000000000000", text = "hello", dateTime = FIXED_ISO)

    attrs.postId shouldBe "00000000-0000-4000-8000-000000000000"
    attrs.text shouldBe "hello"
    attrs.dateTime shouldBe FIXED_ISO
  }

  test(s"should not create post with empty text") {
    val result = Post.create(randomId(), CreatePost(userId = randomId(), text = ""))
    assertLeft(result)
  }

  List(
    "orange",
    "ORANGE",
    "OrAnGe",
    "orange is the new black",
    "I like orange",
    "I like orange, it is my favourite color",
    "I hate ice creams",
    "an orange juice please",
    "I love ice creams",
    "the elephants are so big"
  ).foreach { invalidText =>
    test(s"should not create post with invalid \"$invalidText\" text") {
      val result = Post.create(randomId(), CreatePost(userId = randomId(), text = invalidText))
      assertLeft(result)
    }
  }
}
