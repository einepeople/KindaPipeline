package net.kindapipeline.specs.unit

import java.util.UUID

import monix.eval.Task
import monix.execution.schedulers.TestScheduler
import monix.execution.Scheduler
import net.kindapipeline.algebras.{KafkaAlgebra, KafkaDummy, PutAlgebraImpl}
import net.kindapipeline.model.PutRequest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PutSpec extends AnyFlatSpec with Matchers {
  behavior of "PutAlgebraImpl"
  import PutSpec._
  implicit val sc: Scheduler = TestScheduler()

  it should "correctly process a well-formed request" in {
    implicit val kafka = new KafkaDummy[Task]
    mkAlg.putResource(correct).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 200
        kafka.published.head shouldEqual correct
    }
  }
  it should "fail on unsupported resource type" in {
    implicit val kafka = new KafkaDummy[Task]
    mkAlg.putResource(incorrectResource).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 400
        kafka.published shouldBe empty
    }
  }

  it should "fail on unsupported media type" in {
    implicit val kafka = new KafkaDummy[Task]
    mkAlg.putResource(incorrectMedia).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 415
        kafka.published shouldBe empty
    }
  }

  it should "fail on malformed JSON body" in {
    implicit val kafka = new KafkaDummy[Task]
    mkAlg.putResource(incorrectBody).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 422
        kafka.published shouldBe empty
    }
  }

  it should "fail on JSON body resource not matching resource from URL" in {
    implicit val kafka = new KafkaDummy[Task]

    mkAlg.putResource(incorrectJsonRes).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 422
        kafka.published shouldBe empty
    }
  }

  it should "fail on JSON body ID not matching ID from URL" in {
    implicit val kafka = new KafkaDummy[Task]
    mkAlg.putResource(incorrectJsonId).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 422
        kafka.published shouldBe empty
    }
  }
}

object PutSpec {
  private def toJson(res: String, id: UUID): String = {
    s"""
       |{
       | "resourceType":"$res",
       | "id":"$id"
       |}
       |""".stripMargin
  }
  def allowedResources = List("Foo", "Bar")
  def allowedMediaTypes = List("application/json", "application/fhir+json")
  lazy val correctUUID = UUID.randomUUID()
  def correct = PutRequest("Foo", correctUUID, "application/json", toJson("Foo", correctUUID))
  def incorrectResource = correct.copy(resourceType = "Qeq")
  def incorrectMedia = correct.copy(mediaType = "json")
  def incorrectBody = correct.copy(body = correct.body + ":'}")
  def incorrectJsonRes = correct.copy(body = toJson("Bar", correctUUID))
  def incorrectJsonId = correct.copy(body = toJson("Foo", UUID.randomUUID()))
  def mkAlg(implicit da: KafkaAlgebra[Task]) = {
    new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
  }
}
