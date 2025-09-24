package openchat.application

import scala.concurrent.Future

trait Handler[I, O] {
  def handle(input: I): Future[O]
}
