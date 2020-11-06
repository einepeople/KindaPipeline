package net.kindapipeline

import cats.effect.ExitCode
import monix.eval._
import net.kindapipeline.algebras.{DeleteAlgebraImpl, PutAlgebraImpl}
import net.kindapipeline.programs.WebServer
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import org.apache.kafka.clients.KafkaClient

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = {
    val allowedResources = List("Observation", "EpisodeOfCare")
    val allowedMediaTypes = List("application/json", "application/fhir+json", "application/fhir")
    implicit val pa = new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
    implicit val da = new DeleteAlgebraImpl[Task](allowedResources)
    val routes = new WebServer[Task].routes
    BlazeServerBuilder[Task](this.scheduler)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(Router("/" -> routes).orNotFound)
      .resource
      .use { _ =>
        Task {
          println("Serving at 0.0.0.0:8080")
          println("Press any key to exit ...")
          scala.io.StdIn.readLine()
          ExitCode.Success
        }
      }
  }
}
