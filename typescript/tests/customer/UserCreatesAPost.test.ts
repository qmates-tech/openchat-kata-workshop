import { describe, expect, it } from 'vitest'
import request from 'supertest'
import { BASE_URI, generateRandomSuffix } from '../Utils'

describe('A user creates a post', () => {
  it('and sees it on their timeline', async () => {
    const username = `Alice-${generateRandomSuffix()}`
    const password = 'alk8325d'

    const api = request(BASE_URI)

    // create user
    await api.post('/users').send({
      username: username,
      password: password,
      about: 'I love playing the piano and traveling.',
    })

    // login user
    const loginRes = await api.post('/login').send({
      username: username,
      password: password,
    })
    const userId = loginRes.body.id

    // create a post
    const postRes = await api.post(`/users/${userId}/timeline`).send({ text: "Hello, I'm Alice" })

    // look at the timeline
    const timelineRes = await api.get(`/users/${userId}/timeline`)

    const posts: any[] = timelineRes.body
    expect(Array.isArray(posts)).toBe(true)
    expect(posts.length).toBe(1)
    expect(posts[0]).toStrictEqual(postRes.body)
  })
})
