import { beforeAll, describe, expect, it } from 'vitest'
import request from 'supertest'
import { BASE_URI, generateRandomSuffix } from '../../Utils'
import { UUID_PATTERN } from '../../../src/application/wrapper/UuidWrapper'
import { randomUUID } from 'crypto'

describe('Post Creation API', () => {
  let currentUserId: string
  const api = request(BASE_URI)

  beforeAll(async () => {
    const randomUsername = `Alice-${generateRandomSuffix()}`
    const { body } = await api.post('/users').send({
      username: randomUsername,
      password: 'valid_password',
      about: 'I love playing the piano and traveling.',
    })
    currentUserId = body.id
  })

  describe('when a post is created successfully', () => {
    it('should return 201 status code', async () => {
      await api.post(`/users/${currentUserId}/timeline`).send({ text: "Hello, I'm Alice" }).expect(201)
    })

    it('should return response with application/json content type', async () => {
      await api
        .post(`/users/${currentUserId}/timeline`)
        .send({ text: "Hello, I'm Alice" })
        .expect('Content-Type', /application\/json/)
    })

    it('should return the created post in response body', async () => {
      const { body } = await api.post(`/users/${currentUserId}/timeline`).send({ text: "Hello, I'm Alice" })

      expect(body.postId).toMatch(UUID_PATTERN)
      expect(body).haveOwnProperty('text')
    })
  })

  describe('when post creation fails', () => {
    it('should return 404 status code with invalid userId', async () => {
      await api.post(`/users/invalid-uuid/timeline`).send({ text: "Hello, I'm Alice" }).expect(404)
    })

    it('should return 404 status code with user not found', async () => {
      const notFoundUser = randomUUID()
      await api.post(`/users/${notFoundUser}/timeline`).send({ text: "Hello, I'm Alice" }).expect(404)
    })

    it('should return 400 status code when text is missing', async () => {
      await api.post(`/users/${currentUserId}/timeline`).send({}).expect(400)
    })

    it('should return 400 status code when text is empty', async () => {
      await api.post(`/users/${currentUserId}/timeline`).send({ text: '   ' }).expect(400)
    })

    it('should return 400 status code when text contains inappropriate language', async () => {
      await api.post(`/users/${currentUserId}/timeline`).send({ text: 'the orange word is bad!' }).expect(400)
    })
  })
})
