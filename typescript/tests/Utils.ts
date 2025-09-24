import { User } from '../src/domain/aggregates/User'
import { Post } from '../src/domain/entities/Post'

export const BASE_URI = 'http://localhost:3000'

export const generateRandomSuffix = () => Math.random().toString(36).substring(2, 8)

export const DATE_TIME_PATTERN = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?([+-]\d{2}:\d{2}|Z)$/

export const createUserFactory = (posts: Post[] = []) => {
  return User.createFromAttributes({
    userId: '1-2-3-4-5',
    username: 'username',
    password: 'valid password',
    about: 'about',
    posts,
  })
}

export const createUserFactoryWithPosts = () =>
  createUserFactory([
    Post.createFromAttributes({ postId: 'p-1-2-3-4', text: 'text 1', dateTime: '2025-09-13T12:00:00Z' }),
    Post.createFromAttributes({ postId: 'p-5-6-7-8', text: 'text 2', dateTime: '2025-09-13T12:00:00Z' }),
  ])
