import { describe, expect, it } from 'vitest'
import { UsernameService } from '../../../../src/domain/services/UsernameService'
import { UserRepositoryBaseStub } from '../../../test-double/UserRepositoryBaseStub'
import { User } from '../../../../src/domain/aggregates/User'
import { createUserFactory } from '../../../Utils'

describe('UsernameService', () => {
  describe('check if username exists', () => {
    describe('when username does not exist', () => {
      it('should return false', async () => {
        const userRepository = new UserRepositoryStubGetByUsernameNull()
        const usernameService = new UsernameService(userRepository)
        const usernameExists = await usernameService.usernameExists('Alice')

        expect(usernameExists).toStrictEqual(false)
      })
    })

    describe('when username exists', () => {
      it('should return true', async () => {
        const userRepository = new UserRepositoryStubGetByUsernameSomething()
        const usernameService = new UsernameService(userRepository)

        const usernameExists = await usernameService.usernameExists('Alice')

        expect(usernameExists).toStrictEqual(true)
      })
    })

    it('should propagate repository errors', async () => {
      const repo = new UserRepositoryStubThrows()
      const service = new UsernameService(repo)
      await expect(service.usernameExists('Bob')).rejects.toThrowError('boom')
    })
  })
})

class UserRepositoryStubGetByUsernameNull extends UserRepositoryBaseStub {
  getByUsername(username: string): Promise<User | null> {
    return Promise.resolve(null)
  }
}
class UserRepositoryStubGetByUsernameSomething extends UserRepositoryBaseStub {
  getByUsername(username: string): Promise<User | null> {
    return Promise.resolve(createUserFactory())
  }
}
class UserRepositoryStubThrows extends UserRepositoryBaseStub {
  getByUsername(username: string): Promise<User | null> {
    return Promise.reject(new Error('boom'))
  }
}
