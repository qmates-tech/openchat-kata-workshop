import { Config } from '../config'
import type { UserRepository } from '../application/repositories/UserRepository'
import { UserRepositorySqlite } from './repositories/UserRepositorySqlite'
import connect from '@databases/sqlite'
import { UsernameService } from '../domain/services/UsernameService'
import type { UuidWrapper } from '../application/wrapper/UuidWrapper'
import { UuidWrapperNative } from './wrapper/UuidWrapperNative'

export class AppDependencies {
  readonly userRepository: () => UserRepository
  readonly usernameService: () => UsernameService
  readonly uuidWrapper: () => UuidWrapper

  constructor(public readonly config: Config) {
    this.userRepository = lazy(() => new UserRepositorySqlite(connect(this.config.databaseUrl)))
    this.usernameService = lazy(() => new UsernameService(this.userRepository()))
    this.uuidWrapper = lazy(() => new UuidWrapperNative())
  }
}

export const lazy = <T>(factory: () => T) => {
  let instance: T | undefined

  return () => {
    if (!instance) {
      instance = factory()
    }
    return instance as T
  }
}
