package openchat.application.wrapper

import scala.util.matching.Regex

trait UuidWrapper {
  def generateUuid(): String
}

object UuidWrapper {
  val UUID_PATTERN: Regex = """^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$""".r

  def isValidUuid(value: String): Boolean = {
    if (value == null) false
    else UuidWrapper.UUID_PATTERN.pattern.matcher(value).matches()
  }
}
