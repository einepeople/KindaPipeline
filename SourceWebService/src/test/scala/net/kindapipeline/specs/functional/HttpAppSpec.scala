package net.kindapipeline.specs.functional

import java.util.UUID

import monix.eval.Task
import monix.execution.schedulers.TestScheduler
import net.kindapipeline.algebras._
import io.circe.literal._
import org.http4s.implicits._
import net.kindapipeline.programs.WebServer
import org.http4s.{Header, Headers, Method, Request, Uri}
import org.http4s.Method.{DELETE, PUT}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.http4s.circe._

class HttpAppSpec extends AnyFlatSpec with Matchers {
  behavior of "WebServer"
  import HttpAppSpec._
  implicit val sc = TestScheduler()

  it should "process well-formed HTTP PUT request correctly" in {
    val resource = "Foo"
    val id = UUID.randomUUID()
    val headers = Headers.of(Header("content-type", "application/json"))
    val request =
      Request[Task](PUT, Uri(path = s"/$resource/$id"), headers = headers).withEntity(mkJson(resource, id))
    val app = mkApp()
    app(request).runAsync {
      case Left(_) => fail()
      case Right(response) =>
        response.status.code shouldEqual 200
    }
  }

  it should "fail on processing PUT request with unsupported resource type" in {
    val resource = "Doodka"
    val id = UUID.randomUUID()
    val headers = Headers.of(Header("content-type", "application/json"))
    val request =
      Request[Task](PUT, Uri(path = s"/$resource/$id"), headers = headers).withEntity(mkJson(resource, id))
    val app = mkApp()
    app(request).runAsync {
      case Left(_) => fail()
      case Right(response) =>
        response.status.code shouldEqual 400
    }
  }

  it should "fail on PUT request with incorrect media type" in {
    val resource = "Foo"
    val id = UUID.randomUUID()
    val headers = Headers.of(Header("content-type", "text/plain"))
    val request =
      Request[Task](PUT, Uri(path = s"/$resource/$id"), headers = headers).withEntity(mkJson(resource, id))
    val app = mkApp()
    app(request).runAsync {
      case Left(_) => fail()
      case Right(response) =>
        response.status.code shouldEqual 415
    }
  }

  it should "fail on PUT request with empty payload" in {
    val resource = "Foo"
    val id = UUID.randomUUID()
    val headers = Headers.of(Header("content-type", "application/json"))
    val request =
      Request[Task](PUT, Uri(path = s"/$resource/$id"), headers = headers).withEntity("")
    val app = mkApp()
    app(request).runAsync {
      case Left(_) => fail()
      case Right(response) =>
        response.status.code shouldEqual 422
    }
  }

  it should "fail on PUT request with malformed payload" in {
    val resource = "Foo"
    val id = UUID.randomUUID()
    val headers = Headers.of(Header("content-type", "application/json"))
    val request =
      Request[Task](PUT, Uri(path = s"/$resource/$id"), headers = headers).withEntity(mkJson(resource, id) ++ "}'-*17")
    val app = mkApp()
    app(request).runAsync {
      case Left(_) => fail()
      case Right(response) =>
        response.status.code shouldEqual 422
    }
  }

  it should "fail on PUT request if ID from URL does not match ID from payload" in {
    val resource = "Foo"
    val id = UUID.randomUUID()
    val headers = Headers.of(Header("content-type", "application/json"))
    val request =
      Request[Task](PUT, Uri(path = s"/$resource/$id"), headers = headers)
        .withEntity(mkJson(resource, UUID.randomUUID()))
    val app = mkApp()
    app(request).runAsync {
      case Left(_) => fail()
      case Right(response) =>
        response.status.code shouldEqual 422
    }
  }

  it should "fail on PUT request if Resource from URL doesn't match Resource from payload" in {
    val resource = "Foo"
    val id = UUID.randomUUID()
    val headers = Headers.of(Header("content-type", "application/json"))
    val request =
      Request[Task](PUT, Uri(path = s"/$resource/$id"), headers = headers).withEntity(mkJson("Bar", id))
    val app = mkApp()
    app(request).runAsync {
      case Left(_) => fail()
      case Right(response) =>
        response.status.code shouldEqual 422
    }
  }

  it should "process well-formed HTTP DELETE request correctly" in {
    val resource = "Foo"
    val id = UUID.randomUUID()
    val request =
      Request[Task](DELETE, Uri(path = s"/$resource/$id"))
    val app = mkApp()
    app(request).runAsync {
      case Left(_) => fail()
      case Right(response) =>
        response.status.code shouldEqual 204
    }
  }

  it should "fail on processing DELETE request with unsupported resource type" in {
    val resource = "Amayak_Akopyan"
    val id = UUID.randomUUID()
    val request =
      Request[Task](DELETE, Uri(path = s"/$resource/$id"))
    val app = mkApp()
    app(request).runAsync {
      case Left(_) => fail()
      case Right(response) =>
        response.status.code shouldEqual 400
    }
  }
}

object HttpAppSpec {
  def mkJson(res: String, id: UUID): String = {
    s"""
       |{
       | "resourceType":"$res",
       | "id":"$id",
       | "something":[
       |    "foo":"bar",
       |    "shreck":2
       | ]
       |}
       |""".stripMargin
  }

  def mkApp() = {
    val allowedResources = List("Foo", "Bar")
    val allowedMediaTypes = List("application/json", "application/fhir+json")
    implicit val ka = new KafkaDummy[Task]
    implicit val pa = new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
    implicit val da = new DeleteAlgebraImpl[Task](allowedResources)
    new WebServer[Task].httpApp
  }
}
