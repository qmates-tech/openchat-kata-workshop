package openchat.infrastructure.repositories

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import doobie.Transactor
import doobie.implicits._
import openchat.application.repositories.UserRepository
import openchat.domain.aggregates.User
import openchat.domain.entities.Post

import scala.concurrent.{ExecutionContext, Future}

final class UserRepositoryDoobie(xa: Transactor[IO])(implicit ec: ExecutionContext) extends UserRepository {
  private val _init: Unit = (
    sql"""
      CREATE TABLE IF NOT EXISTS user (
        id       INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id  VARCHAR NOT NULL UNIQUE,
        username VARCHAR NOT NULL UNIQUE,
        password VARCHAR NOT NULL,
        about    TEXT    NOT NULL
      );
    """.update.run *>
      sql"""
      CREATE TABLE IF NOT EXISTS post (
        id              INTEGER PRIMARY KEY AUTOINCREMENT,
        post_id         VARCHAR NOT NULL UNIQUE,
        internal_user_id INTEGER NOT NULL,
        text            TEXT    NOT NULL,
        dateTime        VARCHAR NOT NULL,
        FOREIGN KEY(internal_user_id) REFERENCES user(id)
      );
    """.update.run
  ).transact(xa).unsafeRunSync()

  // Upsert user row and return the internal numeric id
  private def upsertUserReturningInternalId(u: User): IO[Long] = {
    sql"""
      INSERT INTO user(user_id, username, password, about)
      VALUES (${u.userId}, ${u.username}, ${u.password}, ${u.about})
      ON CONFLICT(user_id) DO UPDATE SET
        username = excluded.username,
        password = excluded.password,
        about    = excluded.about
      RETURNING id
    """.query[Long].unique.transact(xa)
  }

  private def selectUserByUsername(username: String): IO[Option[User]] = {
    val q = sql"SELECT user_id, username, about, password FROM user WHERE username = $username"
    q.query[(String, String, String, String)].option.transact(xa).map {
      case None                          => None
      case Some((id, uname, about, pwd)) =>
        Some(User(userId = id, username = uname, about = about, password = pwd, posts = Vector.empty))
    }
  }

  private def selectUser(userId: String): IO[Option[User]] = {
    val q = sql"SELECT user_id, username, about, password FROM user WHERE user_id = $userId"
    q.query[(String, String, String, String)].option.transact(xa).flatMap {
      case None                          => IO.pure(None)
      case Some((id, uname, about, pwd)) =>
        loadPostsInternalUserId(id).map { posts =>
          Some(User(userId = id, username = uname, about = about, password = pwd, posts = posts))
        }
    }
  }

  private def insertOrUpdatePost(internalUserId: Long, post: Post): IO[Unit] = {
    sql"""
      INSERT INTO post(post_id, internal_user_id, text, dateTime)
      VALUES (${post.postId}, $internalUserId, ${post.text}, ${post.dateTime})
      ON CONFLICT(post_id) DO UPDATE SET
        text = excluded.text,
        dateTime = excluded.dateTime
    """.update.run.void.transact(xa)
  }

  private def loadPostsInternalUserId(internalUserId: String): IO[Vector[Post]] = {
    // When called from selectUser above, we pass the external user_id (UUID string), so find internal id first
    sql"SELECT id FROM user WHERE user_id = $internalUserId".query[Long].option.transact(xa).flatMap {
      case None      => IO.pure(Vector.empty)
      case Some(uid) =>
        sql"SELECT post_id, text, dateTime FROM post WHERE internal_user_id = $uid ORDER BY dateTime DESC"
          .query[(String, String, String)]
          .to[Vector]
          .transact(xa)
          .map(_.map { case (pid, text, dt) => Post(postId = pid, text = text, dateTime = dt) })
    }
  }

  override def save(user: User): Future[Unit] = {
    val io = for {
      internalId <- upsertUserReturningInternalId(user)
      _          <- user.posts.traverse_(p => insertOrUpdatePost(internalId, p))
    } yield ()
    io.unsafeToFuture()
  }
  override def getByUsername(username: String): Future[Option[User]] = selectUserByUsername(username).unsafeToFuture()
  override def get(userId: String): Future[Option[User]]             = selectUser(userId).unsafeToFuture()
}
