package net.kindapipeline.programs

import cats.Monad
import cats.syntax._
import cats.effect.{Concurrent, ContextShift, Timer}
import net.kindapipeline.algebras.{DeleteAlgebra, KafkaAlgebra, PutAlgebra}
import net.kindapipeline.routes.Routes.{deleteEndpoint, _}
import sttp.tapir.server.http4s._
import sttp.model.StatusCode
import org.http4s.HttpRoutes
import cats.implicits._

class WebServer[F[+_]: ContextShift: Timer: Concurrent](implicit pa: PutAlgebra[F], da: DeleteAlgebra[F], M: Monad[F]) {
  def routes: HttpRoutes[F] =
    putEndpoint.toRoutes { rq =>
      M.map(pa.putResource(rq)) { res => (res, StatusCode(res.status)).asRight[Unit] }
    } <+> deleteEndpoint.toRoutes { rq => M.map(da.delete(rq)) { _.asRight[Unit] } }

}
