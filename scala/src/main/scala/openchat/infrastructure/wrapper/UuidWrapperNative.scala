package openchat.infrastructure.wrapper

import openchat.application.wrapper.UuidWrapper

import java.util.UUID

final class UuidWrapperNative extends UuidWrapper {
  override def generateUuid(): String = UUID.randomUUID().toString
}
