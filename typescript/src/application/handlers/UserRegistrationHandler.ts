import type { Handler } from '../Handler'
import type { UserRepository } from '../repositories/UserRepository'
import type { UsernameService } from '../../domain/services/UsernameService'
import type { UuidWrapper } from '../wrapper/UuidWrapper'
import type { CreateUser } from '../../domain/commands/CreateUser'
import { User } from '../../domain/aggregates/User'

type UserId = string

export class UserRegistrationHandler implements Handler<CreateUser, UserCreated> {
  constructor(
    private usernameService: UsernameService,
    private userRepository: UserRepository,
    private uuidWrapper: UuidWrapper
  ) {}

  handle = async (createUser: CreateUser) => {
    if (await this.usernameService.usernameExists(createUser.username)) {
      throw new UsernameAlreadyInUseError()
    }

    const user = User.create(this.uuidWrapper.generateUuid(), createUser)
    await this.userRepository.save(user)

    return {
      id: user.userId,
      username: createUser.username,
      about: createUser.about,
    }
  }
}

export type UserCreated = {
  id: UserId
  username: string
  about: string
}

export class UsernameAlreadyInUseError extends Error {}
