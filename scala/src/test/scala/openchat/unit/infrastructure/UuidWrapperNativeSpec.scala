package openchat.unit.infrastructure

import openchat.application.wrapper.UuidWrapper
import openchat.infrastructure.wrapper.UuidWrapperNative
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class UuidWrapperNativeSpec extends AnyFunSuite with Matchers with TableDrivenPropertyChecks {
  private val uuidWrapper = new UuidWrapperNative()

  test("generateUuid should generate a valid UUID v4") {
    val uuid = uuidWrapper.generateUuid()

    uuid should fullyMatch regex UuidWrapper.UUID_PATTERN
    uuid shouldBe a[String]
    uuid should have length 36
  }

  test("generateUuid should generate unique UUIDs on multiple calls") {
    val uuid1 = uuidWrapper.generateUuid()
    val uuid2 = uuidWrapper.generateUuid()

    uuid1 should not be uuid2
  }

  test("generateUuid should generate UUIDs in correct format") {
    val uuid = uuidWrapper.generateUuid()

    // UUID v4 format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
    val parts = uuid.split("-")
    parts should have length 5
    parts(0) should have length 8
    parts(1) should have length 4
    parts(2) should have length 4
    parts(2).charAt(0) shouldBe '4' // UUID version 4
    parts(3) should have length 4
    parts(4) should have length 12
  }

  test("isValidUuid should return true for valid UUID v4") {
    val validUuid = "550e8400-e29b-41d4-a716-446655440000"

    UuidWrapper.isValidUuid(validUuid) shouldBe true
  }

  test("isValidUuid should return true for generated UUID") {
    val generatedUuid = uuidWrapper.generateUuid()

    UuidWrapper.isValidUuid(generatedUuid) shouldBe true
  }

  test("isValidUuid should return false for invalid UUID formats") {
    val invalidUuids = Table(
      ("description", "uuid"),
      ("empty string", ""),
      ("non-UUID string", "not-a-uuid"),
      ("incomplete UUID", "550e8400-e29b-41d4-a716"),
      ("UUID with extra parts", "550e8400-e29b-41d4-a716-446655440000-extra"),
      ("UUID without hyphens", "550e8400e29b41d4a716446655440000"),
      ("UUID with invalid character g", "550e8400-e29b-41d4-a716-44665544000g"),
      ("UUID with invalid character z", "550e8400-e29b-41d4-z716-446655440000")
    )
    forAll(invalidUuids) { (description: String, invalidUuid: String) =>
    withClue(s"Testing $description: ") {
        UuidWrapper.isValidUuid(invalidUuid) shouldBe false
      }
    }
  }

  test("isValidUuid should return false for null") {
    UuidWrapper.isValidUuid(null) shouldBe false
  }

  test("isValidUuid should work with other UUID versions if they match the pattern") {
    val otherVersionUuids = Table(
      ("description", "uuid"),
      ("UUID v1", "550e8400-e29b-11d4-a716-446655440000"),
      ("UUID v3", "550e8400-e29b-31d4-a716-446655440000"),
      ("UUID v5", "550e8400-e29b-51d4-a716-446655440000")
    )
    forAll(otherVersionUuids) { (description: String, uuid: String) =>
    withClue(s"Testing $description: ") {
        UuidWrapper.isValidUuid(uuid) shouldBe false
      }
    }
  }
}