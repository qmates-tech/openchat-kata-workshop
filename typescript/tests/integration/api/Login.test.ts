import { beforeAll, describe, expect, it } from 'vitest'
import request from 'supertest'
import { BASE_URI, generateRandomSuffix } from '../../Utils'
import { UUID_PATTERN } from '../../../src/application/wrapper/UuidWrapper'

describe('Login API', () => {
  let randomUsername: string
  const api = request(BASE_URI)

  beforeAll(async () => {
    randomUsername = `Alice-${generateRandomSuffix()}`
    await api.post('/users').send({
      username: randomUsername,
      password: 'valid_password',
      about: 'I love playing the piano and traveling.',
    })
  })

  describe('when login succeeds', () => {
    it('should return 200 status code', async () => {
      await api.post('/login').send({ username: randomUsername, password: 'valid_password' }).expect(200)
    })

    it('should return response with application/json content type', async () => {
      await api
        .post('/login')
        .send({ username: randomUsername, password: 'valid_password' })
        .expect('Content-Type', /application\/json/)
    })

    it('should return user data without password in response body', async () => {
      const { body } = await api.post('/login').send({ username: randomUsername, password: 'valid_password' })

      expect(body.id).toMatch(UUID_PATTERN)
      expect(body).not.haveOwnProperty('password')
    })
  })

  describe('when fails due to invalid credentials', () => {
    it('should return 401 status code', async () => {
      await api.post('/login').send({ username: randomUsername, password: 'a_password' }).expect(401)
    })
  })
})
