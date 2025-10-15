package openchat.unit.domain.aggregates

import openchat.domain.aggregates.User
import openchat.domain.commands.CreateUser
import openchat.domain.entities.Post
import openchat.infrastructure.wrapper.UuidWrapperNative
import openchat.utilities.Utils.createUser
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class UserSpec extends AnyFunSuite with Matchers {

  test("login should return true for valid password") {
    val user = createUser()

    user.login("valid password") shouldBe true
  }

  test("login should return false for invalid password") {
    val user = createUser()

    user.login("invalid password") shouldBe false
  }

  test("login should return false for empty password") {
    val user = createUser()

    user.login("") shouldBe false
  }

  test("create preserves attributes as-is (including blank/space about)") {
    val id      = (new UuidWrapperNative).generateUuid()
    val created = User.create(id, CreateUser(username = "alice", password = "secret", about = "  "))

    created.userId shouldBe id
    created.username shouldBe "alice"
    created.password shouldBe "secret"
    created.about shouldBe "  "
    created.posts shouldBe empty
  }

  test("addPost should add a post to the user posts array") {
    val user = createUser()
    val post = Post(
      postId = "post-1-2-3-4",
      text = "This is a test post",
      dateTime = "2025-09-13T12:00:00Z"
    )

    val updatedUser = user.addPost(post)

    updatedUser.posts should have length 1
    updatedUser.posts(0) shouldBe post
  }

  test("addPost should add multiple posts to the user posts array") {
    val user  = createUser()
    val post1 = Post(
      postId = "post-1-2-3-4",
      text = "First test post",
      dateTime = "2025-09-13T12:00:00Z"
    )
    val post2 = Post(
      postId = "post-5-6-7-8",
      text = "Second test post",
      dateTime = "2025-09-13T13:00:00Z"
    )

    val userWithPost1     = user.addPost(post1)
    val userWithBothPosts = userWithPost1.addPost(post2)

    userWithBothPosts.posts should have length 2
    userWithBothPosts.posts(0) shouldBe post1
    userWithBothPosts.posts(1) shouldBe post2
  }

  test("addPost should add posts to existing posts array") {
    val existingPost = Post(
      postId = "p-1-2-3-4",
      text = "Existing post",
      dateTime = "2025-09-13T11:00:00Z"
    )
    val user    = createUser(Vector(existingPost))
    val newPost = Post(
      postId = "p-5-6-7-8",
      text = "New post",
      dateTime = "2025-09-13T12:00:00Z"
    )

    val updatedUser = user.addPost(newPost)

    updatedUser.posts should have length 2
    updatedUser.posts(0) shouldBe existingPost
    updatedUser.posts(1) shouldBe newPost
  }

  test("addPost allows duplicate postId entries (documented behavior)") {
    val user   = createUser()
    val sameId = "33333333-3333-4333-8333-333333333333"
    val dup1   = Post(postId = sameId, text = "same id 1", dateTime = "2025-01-01T00:00:00.000Z")
    val dup2   = Post(postId = sameId, text = "same id 2", dateTime = "2025-01-02T00:00:00.000Z")

    val userWithDup1     = user.addPost(dup1)
    val userWithBothDups = userWithDup1.addPost(dup2)

    userWithBothDups.posts should have length 2
    userWithBothDups.posts(0).postId shouldBe sameId
    userWithBothDups.posts(1).postId shouldBe sameId
  }
}
