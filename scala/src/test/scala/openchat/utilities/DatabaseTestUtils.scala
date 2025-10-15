package openchat.utilities

import cats.effect.IO
import doobie.Transactor
import org.scalatest.{BeforeAndAfterAll, Suite}

import java.sql.DriverManager
import scala.collection.mutable.ListBuffer

trait DatabaseTestUtils extends BeforeAndAfterAll {
  self: Suite =>

  private val driver = "org.sqlite.JDBC"
  // Keep-alive connections to named in-memory SQLite DBs so they persist across connections within each test
  private val keepers: ListBuffer[java.sql.Connection] = ListBuffer.empty

  protected def newXa(): Transactor[IO] = {
    // Use a named in-memory database with a shared cache; it lives as long as at least one connection stays open
    val url    = s"jdbc:sqlite:file:memdb-${java.util.UUID.randomUUID().toString}?mode=memory&cache=shared"
    val keeper = DriverManager.getConnection(url)
    keepers += keeper
    Transactor.fromDriverManager[IO](
      driver = driver,
      url = url,
      logHandler = None
    )
  }

  override def afterAll(): Unit = {
    // Close all keeper connections to release memory DBs
    keepers.foreach(c =>
      try c.close()
      catch { case _: Throwable => () }
    )
    keepers.clear()
    super.afterAll()
  }
}
