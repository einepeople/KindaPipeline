package net.kindapipeline

import cats.effect.ExitCode
import monix.eval._
import net.kindapipeline.algebras.{DeleteAlgebraImpl, KafkaDummy, PutAlgebraImpl}
import net.kindapipeline.programs.WebServer
import net.kindapipeline.routes.Routes
import org.http4s.server.blaze.BlazeServerBuilder
import org.apache.kafka.clients.KafkaClient

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = {
    val allowedResources = List("Observation", "EpisodeOfCare")
    val allowedMediaTypes = List("application/json", "application/fhir+json", "application/fhir")
    implicit val ka = new KafkaDummy[Task]
    implicit val pa = new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
    implicit val da = new DeleteAlgebraImpl[Task](allowedResources)
    val httpApp = new WebServer[Task].httpApp
    BlazeServerBuilder[Task](this.scheduler)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
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
