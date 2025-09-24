import type { Handler } from '../Handler'
import type { UserId } from '../../domain/aggregates/User'
import type { PostId } from '../../domain/entities/Post'
import type { UserRepository } from '../repositories/UserRepository'

export class TimelineUserNotFound extends Error {}

type TimelineQuery = { userId: UserId }

type TimelinePost = { postId: PostId; userId: UserId; text: string; dateTime: string }

export class TimelineQueryHandler implements Handler<TimelineQuery, TimelinePost[]> {
  constructor(private readonly userRepository: UserRepository) {}

  async handle({ userId }: TimelineQuery): Promise<TimelinePost[]> {
    const user = await this.userRepository.get(userId)
    if (!user) throw new TimelineUserNotFound()
    return user.posts.map((p) => ({ ...p, userId }))
  }
}
