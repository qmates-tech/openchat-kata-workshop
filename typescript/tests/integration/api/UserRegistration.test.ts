import { describe, expect, it } from 'vitest'
import request from 'supertest'
import { BASE_URI, generateRandomSuffix } from '../../Utils'
import { UUID_PATTERN } from '../../../src/application/wrapper/UuidWrapper'

describe('User Registration API', () => {
  const api = request(BASE_URI)

  describe('when registration is successful', () => {
    it('should return 201 status code', async () => {
      await api
        .post('/users')
        .send({
          username: `Alice-${generateRandomSuffix()}`,
          password: 'alk8325d',
          about: 'I love playing the piano and traveling.',
        })
        .expect(201)
    })

    it('should return response with application/json content type', async () => {
      await api
        .post('/users')
        .send({
          username: `Alice-${generateRandomSuffix()}`,
          password: 'alk8325d',
          about: 'I love playing the piano and traveling.',
        })
        .expect('Content-Type', /application\/json/)
    })

    it('should return user data without password in response body', async () => {
      const username = `Alice-${generateRandomSuffix()}`

      const { body } = await api.post('/users').send({
        username: username,
        password: 'alk8325d',
        about: 'I love playing the piano and traveling.',
      })

      expect(body.id).toMatch(UUID_PATTERN)
      expect(body.username).toBeTypeOf('string')
      expect(body).not.haveOwnProperty('password')
    })
  })

  describe('when username already exists', () => {
    it('should return 400 status code', async () => {
      const username = `Alice-${generateRandomSuffix()}`
      await api
        .post('/users')
        .send({
          username: username,
          password: 'alk8325d',
          about: 'I love playing the piano and traveling.',
        })
        .expect(201)

      await api
        .post('/users')
        .send({
          username: username,
          password: 'bml9436e',
          about: 'I love playing the guitar and stay at home.',
        })
        .expect(400)
    })
  })
})
