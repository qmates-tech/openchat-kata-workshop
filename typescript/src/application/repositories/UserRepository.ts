import type { User, UserId } from '../../domain/aggregates/User'

export interface UserRepository {
  save(user: User): Promise<void>
  get(userId: UserId): Promise<User | null>
  getByUsername(username: string): Promise<User | null>
}
