import { beforeAll, describe, expect, it } from 'vitest'
import request from 'supertest'
import { BASE_URI, DATE_TIME_PATTERN, generateRandomSuffix } from '../../Utils'
import { randomUUID } from 'crypto'

describe('Timeline API', () => {
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

  describe('when fetching timeline successfully', () => {
    it('should return 200 when fetching timeline', async () => {
      await api.get(`/users/${currentUserId}/timeline`).expect(200)
    })

    it('should return response with application/json content type', async () => {
      await api.get(`/users/${currentUserId}/timeline`).expect('Content-Type', /application\/json/)
    })

    it('should return [] when user has no posts', async () => {
      const { body: user } = await request(BASE_URI)
        .post('/users')
        .send({
          username: `Empty-${generateRandomSuffix()}`,
          password: 'pwd',
          about: 'about',
        })

      const res = await api.get(`/users/${user.id}/timeline`)

      expect(res.body).toEqual([])
    })

    it('should return posts when a post exists', async () => {
      await api.post(`/users/${currentUserId}/timeline`).send({ text: 'hello from api' })

      const res = await api.get(`/users/${currentUserId}/timeline`)

      expect(Array.isArray(res.body)).toBe(true)
      expect(res.body.length).toBe(1)
      const item = res.body[0]
      expect(item.userId).toBe(currentUserId)
      expect(item.dateTime).toMatch(DATE_TIME_PATTERN)
    })
  })

  describe('when fetching timeline fails', () => {
    it('should return 404 status code with invalid userId', async () => {
      await api.get(`/users/invalid-uuid/timeline`).expect(404)
    })

    it('should return 404 status code when user does not exist', async () => {
      const notFoundUser = randomUUID()
      await api.get(`/users/${notFoundUser}/timeline`).expect(404)
    })
  })
})
