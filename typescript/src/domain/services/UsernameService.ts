import type { UserRepository } from '../../application/repositories/UserRepository'

export class UsernameService {
  constructor(private userRepository: UserRepository) {}

  async usernameExists(username: string): Promise<boolean> {
    return (await this.userRepository.getByUsername(username)) !== null
  }
}
