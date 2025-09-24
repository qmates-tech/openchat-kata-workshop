import type { Handler } from '../Handler'
import type { UserRepository } from '../repositories/UserRepository'
import type { UserId } from '../../domain/aggregates/User'

export class LoginHandler implements Handler<LoginUser, UserLoggedIn> {
  constructor(private userRepository: UserRepository) {}

  handle = async (loginUser: LoginUser): Promise<UserLoggedIn> => {
    const user = await this.userRepository.getByUsername(loginUser.username)
    if (user === null) return null

    return user.login(loginUser.password)
      ? {
          id: user.userId,
          username: user.username,
          about: user.about,
        }
      : null
  }
}

export type LoginUser = {
  username: string
  password: string
}

export type UserLoggedIn = {
  id: UserId
  username: string
  about: string
} | null
