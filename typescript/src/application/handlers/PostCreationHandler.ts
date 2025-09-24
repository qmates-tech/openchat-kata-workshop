import type { Handler } from '../Handler'
import type { UuidWrapper } from '../wrapper/UuidWrapper'
import type { CreatePost } from '../../domain/commands/CreatePost'
import { UserId } from '../../domain/aggregates/User'
import { Post, PostId } from '../../domain/entities/Post'
import { UserRepository } from '../repositories/UserRepository'

export class PostCreationHandler implements Handler<CreatePost, PostCreated> {
  constructor(
    private readonly uuidWrapper: UuidWrapper,
    private readonly userRepository: UserRepository
  ) {}

  async handle(createPost: CreatePost): Promise<PostCreated> {
    const user = await this.userRepository.get(createPost.userId)
    if (!user) {
      throw new UserNotFound()
    }

    const post = Post.create(this.uuidWrapper.generateUuid(), createPost)
    user.addPost(post)

    await this.userRepository.save(user)

    return Promise.resolve({
      postId: post.postId,
      userId: createPost.userId,
      text: post.text,
      dateTime: post.dateTime,
    })
  }
}

export class UserNotFound extends Error {}

type PostCreated = {
  postId: PostId
  userId: UserId
  text: string
  dateTime: string
}
