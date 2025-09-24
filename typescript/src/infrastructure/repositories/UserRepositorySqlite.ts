import type { UserRepository } from '../../application/repositories/UserRepository'
import { User, type UserAttributes, type UserId } from '../../domain/aggregates/User'
import { type DatabaseConnection, sql } from '@databases/sqlite'
import { Post } from '../../domain/entities/Post'

export class UserRepositorySqlite implements UserRepository {
  private readonly prepared: Promise<void>

  constructor(private db: DatabaseConnection) {
    this.prepared = this.prepare()
  }

  private async prepare() {
    await this.db.query(sql`
      CREATE TABLE IF NOT EXISTS user
      (
        id       INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id  VARCHAR NOT NULL UNIQUE,
        username VARCHAR NOT NULL UNIQUE,
        password VARCHAR NOT NULL,
        about    TEXT    NOT NULL
      );
    `)

    await this.db.query(sql`
      CREATE TABLE IF NOT EXISTS post
      (
        id               INTEGER PRIMARY KEY AUTOINCREMENT,
        post_id          VARCHAR NOT NULL UNIQUE,
        internal_user_id INTEGER NOT NULL,
        text             TEXT    NOT NULL,
        dateTime         VARCHAR NOT NULL,
        FOREIGN KEY (internal_user_id) REFERENCES user (id)
      );
    `)
  }

  async save(user: User): Promise<void> {
    await this.prepared

    await this.db.tx(async (transaction) => {
      const result = await transaction.query(sql`INSERT INTO user (user_id, username, password, about)
                                  VALUES (${user.userId}, ${user.username}, ${user.password}, ${user.about})
                                  ON CONFLICT(user_id) DO UPDATE
                                    SET username = EXCLUDED.username,
                                        password = EXCLUDED.password,
                                        about    = EXCLUDED.about
                                  RETURNING id`)

      const internalUserId = result[0].id
      for (const post of user.posts) {
        await transaction.query(sql`INSERT INTO post (internal_user_id, post_id, text, dateTime)
                                    VALUES (${internalUserId}, ${post.postId}, ${post.text}, ${post.dateTime})
                                    ON CONFLICT(post_id) DO UPDATE
                                      SET text     = EXCLUDED.text,
                                          dateTime = EXCLUDED.dateTime`)
      }
    })
  }

  async get(userId: UserId): Promise<User | null> {
    await this.prepared

    const results = await this.db.query(sql`
      SELECT u.user_id, u.username, u.password, u.about, p.post_id, p.text, p.dateTime
      FROM user u
             LEFT JOIN post p ON u.id = p.internal_user_id
      WHERE u.user_id = ${userId}
      ORDER BY p.dateTime DESC
    `)

    if (results.length === 0) return null

    const userAttributes: UserAttributes = {
      userId: results[0].user_id,
      username: results[0].username,
      password: results[0].password,
      about: results[0].about,
      posts: results
        .filter((row) => row.post_id !== null)
        .map((row) =>
          Post.createFromAttributes({
            postId: row.post_id,
            text: row.text,
            dateTime: row.dateTime,
          })
        ),
    }

    return User.createFromAttributes(userAttributes)
  }

  async getByUsername(username: string): Promise<User | null> {
    await this.prepared

    const results = await this.db.query(
      sql`SELECT user_id, username, password, about FROM user WHERE user.username = ${username}`
    )

    return results[0]
      ? User.createFromAttributes({
          userId: results[0].user_id,
          username: results[0].username,
          password: results[0].password,
          about: results[0].about,
          posts: [],
        })
      : null
  }
}
