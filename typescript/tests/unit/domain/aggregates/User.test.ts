import { describe, expect, it } from 'vitest'
import { createUserFactory } from '../../../Utils'
import { Post } from '../../../../src/domain/entities/Post'
import { User } from '../../../../src/domain/aggregates/User'
import { UuidWrapperNative } from '../../../../src/infrastructure/wrapper/UuidWrapperNative'

describe('User', () => {
  describe('login', () => {
    it('should return true for valid password', () => {
      const user = createUserFactory()

      expect(user.login('valid password')).toBe(true)
    })

    it('should return false for invalid password', () => {
      const user = createUserFactory()

      expect(user.login('invalid password')).toBe(false)
    })

    it('should return false for empty password', () => {
      const user = createUserFactory()

      expect(user.login('')).toBe(false)
    })
  })

  describe('create and createFromAttributes', () => {
    it('create preserves attributes as-is (including blank/space about)', () => {
      const id = new UuidWrapperNative().generateUuid()

      const created = User.create(id, { username: 'alice', password: 'secret', about: '  ' })

      expect(created.userId).toBe(id)
      expect(created.username).toBe('alice')
      expect(created.password).toBe('secret')
      expect(created.about).toBe('  ')
      expect(created.posts).toEqual([])
    })

    it('createFromAttributes preserves posts and order', () => {
      const p1 = Post.createFromAttributes({ postId: '11111111-1111-4111-8111-111111111111', text: 'first', dateTime: '2025-01-01T00:00:00.000Z' })
      const p2 = Post.createFromAttributes({ postId: '22222222-2222-4222-8222-222222222222', text: 'second', dateTime: '2025-01-02T00:00:00.000Z' })
      const id = new UuidWrapperNative().generateUuid()
      const attrs = { userId: id, username: 'u', about: '', password: 'pwd', posts: [p1, p2] }
      const user = User.createFromAttributes(attrs)
      expect(user.userId).toBe(id)
      expect(user.username).toBe('u')
      expect(user.about).toBe('')
      expect(user.password).toBe('pwd')
      expect(user.posts).toEqual([p1, p2])
    })
  })

  describe('addPost', () => {
    it('should add a post to the user posts array', () => {
      const user = createUserFactory()
      const post = Post.createFromAttributes({
        postId: 'post-1-2-3-4',
        text: 'This is a test post',
        dateTime: '2025-09-13T12:00:00Z',
      })

      user.addPost(post)

      expect(user.posts).toHaveLength(1)
      expect(user.posts[0]).toEqual(post)
    })

    it('should add multiple posts to the user posts array', () => {
      const user = createUserFactory()
      const post1 = Post.createFromAttributes({
        postId: 'post-1-2-3-4',
        text: 'First test post',
        dateTime: '2025-09-13T12:00:00Z',
      })
      const post2 = Post.createFromAttributes({
        postId: 'post-5-6-7-8',
        text: 'Second test post',
        dateTime: '2025-09-13T13:00:00Z',
      })

      user.addPost(post1)
      user.addPost(post2)

      expect(user.posts).toHaveLength(2)
      expect(user.posts[0]).toEqual(post1)
      expect(user.posts[1]).toEqual(post2)
    })

    it('should add posts to existing posts array', () => {
      const existingPost = Post.createFromAttributes({
        postId: 'p-1-2-3-4',
        text: 'Existing post',
        dateTime: '2025-09-13T11:00:00Z',
      })
      const user = createUserFactory([existingPost])
      const newPost = Post.createFromAttributes({
        postId: 'p-5-6-7-8',
        text: 'New post',
        dateTime: '2025-09-13T12:00:00Z',
      })

      user.addPost(newPost)

      expect(user.posts).toHaveLength(2)
      expect(user.posts[0]).toEqual(existingPost)
      expect(user.posts[1]).toEqual(newPost)
    })

    it('allows duplicate postId entries (documented behavior)', () => {
      const user = createUserFactory()
      const sameId = '33333333-3333-4333-8333-333333333333'
      const dup1 = Post.createFromAttributes({ postId: sameId, text: 'same id 1', dateTime: '2025-01-01T00:00:00.000Z' })
      const dup2 = Post.createFromAttributes({ postId: sameId, text: 'same id 2', dateTime: '2025-01-02T00:00:00.000Z' })
      user.addPost(dup1)
      user.addPost(dup2)
      expect(user.posts).toHaveLength(2)
      expect(user.posts[0].postId).toBe(sameId)
      expect(user.posts[1].postId).toBe(sameId)
    })
  })
})
