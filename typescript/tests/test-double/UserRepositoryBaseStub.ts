import type { UserRepository } from '../../src/application/repositories/UserRepository'
import type { User, UserId } from '../../src/domain/aggregates/User'

export class UserRepositoryBaseStub implements UserRepository {
  get(userId: UserId): Promise<User | null> {
    throw new Error('Method not implemented.')
  }

  getByUsername(username: string): Promise<User | null> {
    throw new Error('Method not implemented.')
  }

  save(user: User): Promise<void> {
    throw new Error('Method not implemented.')
  }
}
