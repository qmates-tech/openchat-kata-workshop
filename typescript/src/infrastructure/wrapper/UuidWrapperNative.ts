import { type Uuid, UUID_PATTERN, type UuidWrapper } from '../../application/wrapper/UuidWrapper'
import { randomUUID } from 'crypto'

export class UuidWrapperNative implements UuidWrapper {
  generateUuid(): Uuid {
    return randomUUID()
  }

  isValidUuid(value: string): value is Uuid {
    return UUID_PATTERN.test(value)
  }
}
