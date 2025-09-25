import { beforeEach, describe, expect, it } from 'vitest'
import { UserRepositorySqlite } from '../../../src/infrastructure/repositories/UserRepositorySqlite'
import connect from '@databases/sqlite'
import { UsernameService } from '../../../src/domain/services/UsernameService'
import { UUID_PATTERN } from '../../../src/application/wrapper/UuidWrapper'
import { UuidWrapperNative } from '../../../src/infrastructure/wrapper/UuidWrapperNative'
import {
  UsernameAlreadyInUseError,
  UserRegistrationHandler,
} from '../../../src/application/handlers/UserRegistrationHandler'
import { User } from '../../../src/domain/aggregates/User'

class FixedUuidWrapper extends UuidWrapperNative {
  constructor(private readonly fixed: string) {
    super()
  }
  override generateUuid(): any {
    return this.fixed as any
  }
}

describe('User Registration Handler', () => {
  let userRepository: UserRepositorySqlite
  let handler: UserRegistrationHandler

  beforeEach(() => {
    userRepository = new UserRepositorySqlite(connect())
    handler = new UserRegistrationHandler(new UsernameService(userRepository), userRepository, new UuidWrapperNative())
  })

  describe('when creating a new user', () => {
    it('should return created user', async () => {
      const result = await handler.handle({
        username: 'Alice',
        password: 'alk8325d',
        about: 'I love playing the piano and traveling.',
      })

      expect(result).toStrictEqual({
        id: expect.stringMatching(UUID_PATTERN),
        username: 'Alice',
        about: 'I love playing the piano and traveling.',
      })
    })

    it('should save user data correctly in repository', async () => {
      const user = await handler.handle({
        username: 'Alice',
        password: 'alk8325d',
        about: 'I love playing the piano and traveling.',
      })

      const storedUser = await userRepository.get(user.id)
      expect(storedUser).toStrictEqual(
        User.createFromAttributes({
          userId: user.id,
          username: 'Alice',
          about: 'I love playing the piano and traveling.',
          password: 'alk8325d',
          posts: [],
        })
      )
    })

    it('uses deterministic UUID when provided and does not leak password', async () => {
      const fixed = '00000000-0000-4000-8000-000000000000'
      const deterministic = new UserRegistrationHandler(
        new UsernameService(userRepository),
        userRepository,
        new FixedUuidWrapper(fixed)
      )

      const result = await deterministic.handle({ username: 'Bob', password: 'secret', about: '  ' })

      expect(result).toStrictEqual({ id: fixed, username: 'Bob', about: '  ' })
      const stored = await userRepository.get(fixed)
      expect(stored?.password).toBe('secret')
      expect(Object.keys(result)).not.toContain('password')
    })
  })

  describe('when username is already in use', () => {
    it('should throw an Error', async () => {
      await handler.handle({
        username: 'Alice',
        password: 'alk8325d',
        about: 'I love playing the piano and traveling.',
      })

      await expect(
        handler.handle({
          username: 'Alice',
          password: 'bml9436e',
          about: 'I love playing the guitar and stay at home.',
        })
      ).rejects.toThrowError(new UsernameAlreadyInUseError())
    })
  })
})
