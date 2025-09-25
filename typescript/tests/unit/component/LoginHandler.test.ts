import { beforeEach, describe, expect, it } from 'vitest'
import { LoginHandler } from '../../../src/application/handlers/LoginHandler'
import { UserRepositorySqlite } from '../../../src/infrastructure/repositories/UserRepositorySqlite'
import connect from '@databases/sqlite'
import { UuidWrapperNative } from '../../../src/infrastructure/wrapper/UuidWrapperNative'
import { User } from '../../../src/domain/aggregates/User'

describe('Login Handler', () => {
  let handler: LoginHandler
  let userRepositorySqlite: UserRepositorySqlite

  beforeEach(() => {
    userRepositorySqlite = new UserRepositorySqlite(connect())
    handler = new LoginHandler(userRepositorySqlite)
  })

  describe('when logging in with valid credentials', () => {
    it('should return the logged in user data', async () => {
      const created = await createUser('Alice', 'valid_password')

      const result = await handler.handle({ username: 'Alice', password: 'valid_password' })

      expect(result).toStrictEqual({
        id: created.userId,
        username: 'Alice',
        about: '',
      })
      expect(result as any).not.toHaveProperty('password')
    })
  })

  describe('when user does not exist', () => {
    it('should return null', async () => {
      const loggedInUser = await handler.handle({
        username: 'not_existing_user',
        password: 'a_password',
      })

      expect(loggedInUser).toBeNull()
    })
  })

  describe('when logging in with invalid credentials', () => {
    it('should return null', async () => {
      await createUser('Alice', 'valid_password')

      const loggedInUser = await handler.handle({
        username: 'Alice',
        password: 'invalid_password',
      })

      expect(loggedInUser).toBeNull()
    })
  })

  const createUser = async (username: string, password: string) => {
    const user = User.createFromAttributes({
      userId: new UuidWrapperNative().generateUuid(),
      username: username,
      password: password,
      about: '',
      posts: [],
    })
    await userRepositorySqlite.save(user)
    return user
  }
})
