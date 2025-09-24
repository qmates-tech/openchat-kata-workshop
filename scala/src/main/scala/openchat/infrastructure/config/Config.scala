package openchat.infrastructure.config

final case class HttpConfig(host: String, port: Int)
final case class DbConfig(url: String, driver: String)
final case class AppConfig(http: HttpConfig, db: DbConfig)

object AppConfig {
  def loadFromEnv(env: Map[String, String] = sys.env): AppConfig = {
    val host   = env.getOrElse("OPENCHAT_HOST", "127.0.0.1")
    val port   = env.get("OPENCHAT_PORT").flatMap(s => scala.util.Try(s.toInt).toOption).getOrElse(8080)
    val dbUrl  = env.getOrElse("OPENCHAT_DB_URL", "jdbc:sqlite:database/database.db")
    val driver = env.getOrElse("OPENCHAT_DB_DRIVER", "org.sqlite.JDBC")

    AppConfig(
      http = HttpConfig(host = host, port = port),
      db = DbConfig(url = dbUrl, driver = driver)
    )
  }
}
