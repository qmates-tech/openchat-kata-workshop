import { beforeEach, describe, expect, it, vi } from 'vitest'
import { PostCreationHandler, UserNotFound } from '../../../src/application/handlers/PostCreationHandler'
import { UUID_PATTERN } from '../../../src/application/wrapper/UuidWrapper'
import { createUserFactory, DATE_TIME_PATTERN } from '../../Utils'
import { UuidWrapperNative } from '../../../src/infrastructure/wrapper/UuidWrapperNative'
import { UserRepositorySqlite } from '../../../src/infrastructure/repositories/UserRepositorySqlite'
import connect from '@databases/sqlite'
import { Post } from '../../../src/domain/entities/Post'

const FIXED_DATE_ISO = '2025-09-15T10:20:30.000Z'

describe('PostCreationHandler', () => {
  let userRepository: UserRepositorySqlite
  const uuidWrapper = new UuidWrapperNative()
  let handler: PostCreationHandler

  beforeEach(() => {
    userRepository = new UserRepositorySqlite(connect())
    handler = new PostCreationHandler(uuidWrapper, userRepository)
  })

  describe('when creating a new post', () => {
    it('should return the created post data', async () => {
      const alreadyExistingUser = createUserFactory()
      await userRepository.save(alreadyExistingUser)

      const result = await handler.handle({ userId: alreadyExistingUser.userId, text: 'a text' })

      expect(result).toStrictEqual({
        postId: expect.stringMatching(UUID_PATTERN),
        userId: alreadyExistingUser.userId,
        text: 'a text',
        dateTime: expect.stringMatching(DATE_TIME_PATTERN),
      })
    })

    it('should save post data correctly in repository', async () => {
      const alreadyExistingUser = createUserFactory()
      await userRepository.save(alreadyExistingUser)

      await handler.handle({ userId: alreadyExistingUser.userId, text: 'a text' })

      const storedUser = await userRepository.get(alreadyExistingUser.userId)
      expect(storedUser?.posts).toStrictEqual([
        Post.createFromAttributes({
          postId: expect.stringMatching(UUID_PATTERN),
          text: 'a text',
          dateTime: expect.stringMatching(DATE_TIME_PATTERN),
        }),
      ])
    })

    it('appends to existing posts without losing any', async () => {
      const user = createUserFactory()
      await userRepository.save(user)

      await handler.handle({ userId: user.userId, text: 'first' })
      await handler.handle({ userId: user.userId, text: 'second' })

      const stored = await userRepository.get(user.userId)
      expect(stored?.posts.length).toBe(2)
      const texts = stored?.posts.map((p) => p.text)
      expect(texts).toEqual(expect.arrayContaining(['first', 'second']))
    })

    it('uses fake clock for deterministic dateTime', async () => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date(FIXED_DATE_ISO))
      try {
        const user = createUserFactory()
        await userRepository.save(user)

        const result = await handler.handle({ userId: user.userId, text: 'time test' })
        expect(result.dateTime).toBe(FIXED_DATE_ISO)

        const stored = await userRepository.get(user.userId)
        expect(stored?.posts[0].dateTime).toBe(FIXED_DATE_ISO)
      } finally {
        vi.useRealTimers()
      }
    })
  })

  describe('when user does not exist', () => {
    it('should throw UserNotFound', async () => {
      await expect(handler.handle({ userId: uuidWrapper.generateUuid(), text: 'missing' })).rejects.toThrowError(
        new UserNotFound()
      )
    })
  })
})
