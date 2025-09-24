package openchat

import openchat.infrastructure.AppDependencies
import openchat.infrastructure.api.Routes
import openchat.infrastructure.config.AppConfig
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem  = ActorSystem("openchat-system")
    implicit val ec: ExecutionContext = system.dispatcher

    val config = AppConfig.loadFromEnv()

    val deps   = new AppDependencies(config)(ec)
    val routes = new Routes(
      deps.userRegistrationHandler,
      deps.loginHandler,
      deps.postCreationHandler,
      deps.timelineQueryHandler
    )(ec).route

    val host = config.http.host
    val port = config.http.port

    val binding: Future[Http.ServerBinding] = Http().newServerAt(host, port).bind(routes)

    binding.foreach { b =>
      val addr = b.localAddress
      println(s"OpenChat HTTP server online at http://${addr.getHostString}:${addr.getPort}/\nPress ENTER to stop...")
    }

    // Keep running until user presses ENTER
    StdIn.readLine()
    binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
