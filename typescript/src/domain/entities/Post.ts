import type { Uuid } from '../../application/wrapper/UuidWrapper'
import type { GetAttributes } from '../../helpers/TypesystemHelper'
import type { CreatePost } from '../commands/CreatePost'

export type PostId = Uuid

const inappropriateWords = ['orange', 'elephant', 'ice cream']

export class Post {
  private constructor(
    public readonly postId: PostId,
    public readonly text: string,
    public readonly dateTime: string
  ) {}

  private isValid(): boolean {
    const lowerText = this.text.toLowerCase()
    return (
      this.text.length > 0 &&
      !inappropriateWords.some(word => lowerText.includes(word))
    )
  }

  static create(postId: PostId, createPost: CreatePost): Post {
    const post = new Post(postId, createPost.text, new Date().toISOString())
    if (!post.isValid()) {
      throw new InvalidPost()
    }
    return post
  }

  static createFromAttributes(attributes: PostAttributes): Post {
    return new Post(attributes.postId, attributes.text, attributes.dateTime)
  }
}

export type PostAttributes = GetAttributes<Post>

export class InvalidPost extends Error {}
