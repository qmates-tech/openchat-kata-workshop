import { UserId } from '../aggregates/User'

export type CreatePost = {
  userId: UserId
  text: string
}
