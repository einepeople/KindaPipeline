package net.kindapipeline.specs.unit

import java.util.UUID

import monix.eval.Task
import monix.execution.schedulers.TestScheduler
import monix.execution.Scheduler
import net.kindapipeline.algebras.{KafkaDummy, PutAlgebraImpl}
import net.kindapipeline.model.PutRequest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PutSpec extends AnyFlatSpec with Matchers {
  behavior of "PutAlgebraImpl"
  import PutSpec._
  it should "correctly process a well-formed request" in {
    implicit val kafka = new KafkaDummy[Task]
    val put = new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
    put.putResource(correct).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 200
        implicitly[KafkaDummy[Task]].published.head shouldEqual correct
    }
  }
  it should "fail on unsupported resource type" in {
    implicit val kafka = new KafkaDummy[Task]
    val put = new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
    put.putResource(incorrectResource).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 400
        implicitly[KafkaDummy[Task]].published shouldBe empty
    }
  }

  it should "fail on unsupported media type" in {
    implicit val kafka = new KafkaDummy[Task]
    val put = new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
    put.putResource(incorrectMedia).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 415
        implicitly[KafkaDummy[Task]].published shouldBe empty
    }
  }

  it should "fail on malformed JSON body" in {
    implicit val kafka = new KafkaDummy[Task]
    val put = new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
    put.putResource(incorrectBody).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 422
        implicitly[KafkaDummy[Task]].published shouldBe empty
    }
  }

  it should "fail on JSON body resource not matching resource from URL" in {
    implicit val kafka = new KafkaDummy[Task]
    val put = new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
    put.putResource(incorrectJsonRes).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 422
        implicitly[KafkaDummy[Task]].published shouldBe empty
    }
  }

  it should "fail on JSON body ID not matching ID from URL" in {
    implicit val kafka = new KafkaDummy[Task]
    val put = new PutAlgebraImpl[Task](allowedResources)(allowedMediaTypes)
    put.putResource(incorrectJsonId).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 422
        implicitly[KafkaDummy[Task]].published shouldBe empty
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
  implicit val sc: Scheduler = TestScheduler()
  val allowedResources = List("Foo", "Bar")
  val allowedMediaTypes = List("application/json", "application/fhir+json")
  val correctUUID = UUID.randomUUID()
  val correct = PutRequest("Foo", correctUUID, "application/json", toJson("Foo", correctUUID))
  val incorrectResource = correct.copy(resourceType = "Qeq")
  val incorrectMedia = correct.copy(mediaType = "json")
  val incorrectBody = correct.copy(body = correct.body + ":}")
  val incorrectJsonRes = correct.copy(body = toJson("Bar", correctUUID))
  val incorrectJsonId = correct.copy(body = toJson("Foo", UUID.randomUUID()))
}
