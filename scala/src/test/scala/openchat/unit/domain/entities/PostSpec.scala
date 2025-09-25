package openchat.unit.domain.entities

import openchat.domain.commands.CreatePost
import openchat.domain.entities.Post
import openchat.infrastructure.wrapper.UuidWrapperNative
import openchat.utilities.Utils.{DATE_TIME_PATTERN, UUID_PATTERN}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

import java.time.{Clock, Instant, ZoneOffset}

class PostSpec extends AnyFunSuite with Matchers with TableDrivenPropertyChecks {
  def newUuid(): String = (new UuidWrapperNative).generateUuid()

  private val FIXED_ISO = "2025-09-15T10:20:30.000Z"

  // Post creation tests
  test("should create a valid Post") {
    val post = Post.create(newUuid(), CreatePost(userId = newUuid(), text = "text"))

    post.isRight shouldBe true
    val createdPost = post.getOrElse(fail("Expected Right but got Left"))
    createdPost.text shouldBe "text"
  }

  test("should have an id in UUID format") {
    val post = Post.create(newUuid(), CreatePost(userId = newUuid(), text = "text"))

    post.isRight shouldBe true
    val createdPost = post.getOrElse(fail("Expected Right but got Left"))
    createdPost.postId should fullyMatch regex UUID_PATTERN
  }

  test("should have a date-time") {
    val post = Post.create(newUuid(), CreatePost(userId = newUuid(), text = "text"))

    post.isRight shouldBe true
    val createdPost = post.getOrElse(fail("Expected Right but got Left"))
    createdPost.dateTime should fullyMatch regex DATE_TIME_PATTERN
  }

  test("uses fake clock to produce deterministic ISO ending with .000Z") {
    val fixedInstant = Instant.parse(FIXED_ISO)
    val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    val post = Post.create(newUuid(), CreatePost(userId = newUuid(), text = "deterministic"), fixedClock)

    post.isRight shouldBe true
    val createdPost = post.getOrElse(fail("Expected Right but got Left"))
    createdPost.dateTime shouldBe FIXED_ISO
    createdPost.dateTime.endsWith(".000Z") shouldBe true
  }

  test("createFromAttributes preserves id/text/dateTime as-is") {
    val fixedId = "00000000-0000-4000-8000-000000000000"
    val attrs   = Post(postId = fixedId, text = "hello", dateTime = FIXED_ISO)

    attrs.postId shouldBe fixedId
    attrs.text shouldBe "hello"
    attrs.dateTime shouldBe FIXED_ISO
  }

  // Invalid post creation tests
  test("should not create post with invalid text") {
    val invalidTexts = Table(
      "text",
      "",
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
    )
    forAll(invalidTexts) { text =>
      val result = Post.create(newUuid(), CreatePost(userId = newUuid(), text = text))
      result.isLeft shouldBe true
    }
  }
}
