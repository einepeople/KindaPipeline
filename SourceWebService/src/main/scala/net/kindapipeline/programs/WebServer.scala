package net.kindapipeline.programs

import cats.Monad
import cats.effect.{Concurrent, ContextShift, Timer}
import net.kindapipeline.algebras.{DeleteAlgebra, PutAlgebra}
import net.kindapipeline.routes.Routes._
import sttp.tapir.server.http4s._
import sttp.model.StatusCode
import org.http4s.HttpApp
import cats.implicits._
import org.http4s.server.Router
import org.http4s.implicits._

class WebServer[F[+_]](
  implicit pa: PutAlgebra[F],
  da: DeleteAlgebra[F],
  M: Monad[F],
  cs: ContextShift[F],
  t: Timer[F],
  conc: Concurrent[F]) {
  def httpApp: HttpApp[F] = {
    val routes = putEndpoint.toRoutes { rq =>
      (pa putResource rq) fmap { res => (res, StatusCode(res.status)).asRight[Unit] }
    } <+> deleteEndpoint.toRoutes { rq => (da delete rq) fmap { _.asRight[Unit] } }
    Router("/" -> routes).orNotFound
  }

}
