import { beforeEach, describe, expect, it } from 'vitest'
import connect from '@databases/sqlite'
import { UserRepositorySqlite } from '../../../src/infrastructure/repositories/UserRepositorySqlite'
import { TimelineQueryHandler, TimelineUserNotFound } from '../../../src/application/handlers/TimelineQueryHandler'
import { UuidWrapperNative } from '../../../src/infrastructure/wrapper/UuidWrapperNative'
import { createUserFactory, createUserWithPostsFactory, DATE_TIME_PATTERN } from '../../Utils'
import { User } from '../../../src/domain/aggregates/User'
import { Post } from '../../../src/domain/entities/Post'

describe('TimelineQueryHandler', () => {
  let userRepository: UserRepositorySqlite
  let handler: TimelineQueryHandler
  const uuid = new UuidWrapperNative()

  beforeEach(() => {
    userRepository = new UserRepositorySqlite(connect())
    handler = new TimelineQueryHandler(userRepository)
  })

  it('returns posts with expected data structure', async () => {
    const user = createUserWithPostsFactory()

    await userRepository.save(user)

    const result = await handler.handle({ userId: user.userId })

    expect(Array.isArray(result)).toBe(true)
    expect(result.length).toBe(2)

    const p = result[0]
    expect(p.postId).toBe('p-1-2-3-4')
    expect(p.userId).toBe(user.userId)
    expect(typeof p.text).toBe('string')
    expect(p.dateTime).toMatch(DATE_TIME_PATTERN)
  })

  it('returns [] when user has no posts', async () => {
    const user = createUserFactory()
    await userRepository.save(user)
    const result = await handler.handle({ userId: user.userId })
    expect(result).toEqual([])
  })

  it('returns posts in reverse chronological order', async () => {
    const userId = new UuidWrapperNative().generateUuid()

    const older = Post.createFromAttributes({
      postId: uuid.generateUuid(),
      text: 'older post',
      dateTime: '2025-09-13T12:00:00.000Z',
    })
    const newer = Post.createFromAttributes({
      postId: uuid.generateUuid(),
      text: 'newer post',
      dateTime: '2025-09-14T12:00:00.000Z',
    })

    const user = User.createFromAttributes({
      userId,
      username: 'alice',
      password: 'secret',
      about: 'about alice',
      posts: [older, newer],
    })

    await userRepository.save(user)

    const result = await handler.handle({ userId })

    expect(result.length).toBe(2)
    expect(result[0].text).toBe('newer post')
    expect(result[1].text).toBe('older post')
  })

  it('throws TimelineUserNotFound when user does not exist', async () => {
    const missingUserId = uuid.generateUuid()
    await expect(handler.handle({ userId: missingUserId })).rejects.toThrowError(new TimelineUserNotFound())
  })
})
