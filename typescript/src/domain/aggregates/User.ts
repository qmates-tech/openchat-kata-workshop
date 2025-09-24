import type { GetAttributes } from '../../helpers/TypesystemHelper'
import type { Uuid } from '../../application/wrapper/UuidWrapper'
import type { CreateUser } from '../commands/CreateUser'
import { Post } from '../entities/Post'

export type UserId = Uuid

export class User {
  private constructor(
    public readonly userId: UserId,
    public readonly username: string,
    public readonly about: string,
    public readonly password: string,
    public readonly posts: Post[] = []
  ) {}

  login(password: string): boolean {
    return password === this.password
  }

  addPost(post: Post) {
    this.posts.push(post)
  }

  static create(userId: UserId, createUser: CreateUser): User {
    return new User(userId, createUser.username, createUser.about, createUser.password, [])
  }

  static createFromAttributes(attributes: UserAttributes): User {
    return new User(attributes.userId, attributes.username, attributes.about, attributes.password, attributes.posts)
  }
}

export type UserAttributes = GetAttributes<User>
