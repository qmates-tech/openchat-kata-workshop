import { describe, expect, it, vi } from 'vitest'
import { DATE_TIME_PATTERN } from '../../../Utils'
import { InvalidPost, Post } from '../../../../src/domain/entities/Post'
import { UuidWrapperNative } from '../../../../src/infrastructure/wrapper/UuidWrapperNative'
import type { CreatePost } from '../../../../src/domain/commands/CreatePost'
import { Uuid, UUID_PATTERN } from '../../../../src/application/wrapper/UuidWrapper'

const FIXED_ISO = '2025-09-15T10:20:30.000Z'

describe('Post', () => {
  describe('creation', () => {
    it('should create a valid Post', () => {
      const post = Post.create(newUuid(), createPostCommandFactory('text'))

      expect(post).instanceof(Post)
      expect(post.text).toStrictEqual('text')
    })

    it('should have an id in UUID format', () => {
      const post = Post.create(newUuid(), createPostCommandFactory('text'))

      expect(post.postId).toMatch(UUID_PATTERN)
    })

    it('should have a date-time', () => {
      const post = Post.create(newUuid(), createPostCommandFactory('text'))

      expect(post.dateTime).toMatch(DATE_TIME_PATTERN)
    })

    it('uses fake clock to produce deterministic ISO ending with .000Z', () => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date(FIXED_ISO))
      try {
        const post = Post.create(newUuid(), createPostCommandFactory('deterministic'))
        expect(post.dateTime).toBe(FIXED_ISO)
        expect(post.dateTime.endsWith('.000Z')).toBe(true)
      } finally {
        vi.useRealTimers()
      }
    })

    it('createFromAttributes preserves id/text/dateTime as-is', () => {
      const fixedId: Uuid = '00000000-0000-4000-8000-000000000000'
      const attrs = { postId: fixedId, text: 'hello', dateTime: FIXED_ISO }
      const post = Post.createFromAttributes(attrs)
      expect(post.postId).toBe(fixedId)
      expect(post.text).toBe('hello')
      expect(post.dateTime).toBe(FIXED_ISO)
    })
  })

  describe('invalid post creation', () => {
    it.each([
      ['empty text', ''],
      ['text is a single inappropriate word', 'orange'],
      ['text is a single inappropriate uppercase word', 'ORANGE'],
      ['text is a single inappropriate mixed case word', 'OrAnGe'],
      ['text starting with an inappropriate word', 'orange is the new black'],
      ['text ending with an inappropriate word', 'I like orange'],
      ['text including an inappropriate word', 'I like orange, it is my favourite color'],
      ['text including an inappropriate composite word', 'I hate ice creams'],
      ['text including orange', 'an orange juice please'],
      ['text including ice cream', 'I love ice creams'],
      ['text including elephant', 'the elephants are so big'],
    ])('should not create post with %s', (_, text) => {
      expect(() => Post.create(newUuid(), createPostCommandFactory(text))).toThrow(new InvalidPost())
    })
  })
})

function createPostCommandFactory(text: string): CreatePost {
  return { userId: newUuid(), text }
}

function newUuid(): Uuid {
  return new UuidWrapperNative().generateUuid()
}
