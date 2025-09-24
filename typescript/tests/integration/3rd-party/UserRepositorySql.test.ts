import { beforeEach, describe, expect, it } from 'vitest'
import { UserRepositorySqlite } from '../../../src/infrastructure/repositories/UserRepositorySqlite'
import connect from '@databases/sqlite'
import { User } from '../../../src/domain/aggregates/User'
import { createUserFactory, createUserFactoryWithPosts, DATE_TIME_PATTERN } from '../../Utils'
import { Post } from '../../../src/domain/entities/Post'

describe('User repository', () => {
  let repository: UserRepositorySqlite

  beforeEach(() => {
    repository = new UserRepositorySqlite(connect())
  })

  describe('save', () => {
    it('should save a user', async () => {
      await repository.save(createUserFactory())

      const user = await repository.get('1-2-3-4-5')
      expect(user?.userId).equal('1-2-3-4-5')
    })

    it('should update an existing user', async () => {
      await repository.save(createUserFactory())

      await repository.save(
        User.createFromAttributes({
          userId: '1-2-3-4-5',
          username: 'username updated',
          password: 'valid password',
          about: 'about updated',
          posts: [],
        })
      )

      const user = await repository.get('1-2-3-4-5')
      expect(user).toStrictEqual(
        User.createFromAttributes({
          userId: '1-2-3-4-5',
          username: 'username updated',
          about: 'about updated',
          password: 'valid password',
          posts: [],
        })
      )
    })

    it('should handle unique username constraint', async () => {
      await repository.save(createUserFactory())

      await expect(
        repository.save(
          User.createFromAttributes({
            userId: '6-7-8-9-0',
            username: 'username',
            password: 'pass2',
            about: 'About user 2',
            posts: [],
          })
        )
      ).rejects.toThrow()
    })
  })

  describe('get', () => {
    describe('when user is not found', () => {
      it('should return null', async () => {
        const user = await repository.get('1-2-3-4-5')

        expect(user).toBeNull()
      })
    })

    describe('when user is found', () => {
      it('should return a saved user', async () => {
        await repository.save(createUserFactory())

        const user = await repository.get('1-2-3-4-5')
        expect(user).toStrictEqual(
          User.createFromAttributes({
            userId: '1-2-3-4-5',
            username: 'username',
            about: 'about',
            password: 'valid password',
            posts: [],
          })
        )
      })

      it('should return a user with posts when posts exist', async () => {
        await repository.save(createUserFactoryWithPosts())
        const retrievedUser = await repository.get('1-2-3-4-5')

        expect(retrievedUser).not.toBeNull()
        expect(retrievedUser?.posts).toHaveLength(2)
        expect(retrievedUser?.posts[0]).toMatchObject({ postId: 'p-1-2-3-4', text: 'text 1' })
        expect(retrievedUser?.posts[1]).toMatchObject({ postId: 'p-5-6-7-8', text: 'text 2' })
      })

      it('should return user with empty posts array when user has no posts', async () => {
        await repository.save(createUserFactory())

        const user = await repository.get('1-2-3-4-5')
        expect(user?.posts).toEqual([])
      })
    })
  })

  describe('getByUsername', () => {
    describe('when user is not found', () => {
      it('should return null', async () => {
        const user = await repository.getByUsername('not_existing_user')

        expect(user).toBeNull()
      })
    })

    describe('when user is found', () => {
      it('should return a saved user', async () => {
        await repository.save(createUserFactory())

        const user = await repository.getByUsername('username')
        expect(user).toStrictEqual(createUserFactory())
      })

      it('should return user without posts (getByUsername does not include posts)', async () => {
        await repository.save(createUserFactoryWithPosts())
        const retrievedUser = await repository.getByUsername('username')

        expect(retrievedUser).not.toBeNull()
        expect(retrievedUser?.posts).toEqual([])
      })
    })
  })

  describe('post-related functionality', () => {
    it('should preserve dateTime when retrieving posts', async () => {
      await repository.save(createUserFactoryWithPosts())
      const retrievedUser = await repository.get('1-2-3-4-5')

      expect(retrievedUser?.posts[0]).toHaveProperty('dateTime')
      expect(retrievedUser?.posts[0].dateTime).toMatch(DATE_TIME_PATTERN)
      expect(retrievedUser?.posts[1].dateTime).toMatch(DATE_TIME_PATTERN)
    })

    it('should return posts ordered by dateTime DESC', async () => {
      const user = createUserFactory([
        Post.createFromAttributes({ postId: 'p-1-2-3-4', text: 'older post', dateTime: '2025-09-12T12:00:00Z' }),
        Post.createFromAttributes({ postId: 'p-5-6-7-8', text: 'newer post', dateTime: '2025-09-14T12:00:00Z' }),
      ])

      await repository.save(user)
      const retrievedUser = await repository.get('1-2-3-4-5')

      expect(retrievedUser?.posts[0].text).toBe('newer post')
      expect(retrievedUser?.posts[1].text).toBe('older post')
    })

    it('should save a user with posts', async () => {
      await repository.save(createUserFactoryWithPosts())

      const savedUser = await repository.get('1-2-3-4-5')
      expect(savedUser?.posts).toHaveLength(2)
      expect(savedUser?.posts[0].text).toBe('text 1')
      expect(savedUser?.posts[1].text).toBe('text 2')
    })

    it('should update user with a new post', async () => {
      await repository.save(
        createUserFactory([
          Post.createFromAttributes({ postId: 'p-1-2-3-4', text: 'text 1', dateTime: '2025-09-13T12:00:00Z' }),
          Post.createFromAttributes({ postId: 'p-5-6-7-8', text: 'text 2', dateTime: '2025-09-13T12:00:00Z' }),
        ])
      )

      await repository.save(
        createUserFactory([
          Post.createFromAttributes({ postId: 'p-1-2-3-4', text: 'text 1', dateTime: '2025-09-13T12:00:00Z' }),
          Post.createFromAttributes({ postId: 'p-5-6-7-8', text: 'text 2', dateTime: '2025-09-13T12:00:00Z' }),
          Post.createFromAttributes({ postId: 'p-2-3-4-5', text: 'text 3', dateTime: '2025-09-13T12:00:00Z' }),
        ])
      )

      const savedUser = await repository.get('1-2-3-4-5')
      expect(savedUser?.posts).toHaveLength(3)
      expect(savedUser?.posts[0].text).toBe('text 1')
      expect(savedUser?.posts[1].text).toBe('text 2')
      expect(savedUser?.posts[2].text).toBe('text 3')
    })

    it('should handle updating existing posts', async () => {
      await repository.save(
        createUserFactory([
          Post.createFromAttributes({ postId: 'p-1-2-3-4', text: 'original text', dateTime: '2025-09-13T12:00:00Z' }),
        ])
      )

      await repository.save(
        createUserFactory([
          Post.createFromAttributes({ postId: 'p-1-2-3-4', text: 'updated text', dateTime: '2025-09-13T13:00:00Z' }),
        ])
      )

      const savedUser = await repository.get('1-2-3-4-5')
      expect(savedUser?.posts[0].text).toBe('updated text')
      expect(savedUser?.posts[0].dateTime).toBe('2025-09-13T13:00:00Z')
    })
  })
})
