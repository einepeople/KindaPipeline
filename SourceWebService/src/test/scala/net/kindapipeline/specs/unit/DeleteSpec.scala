package net.kindapipeline.specs.unit

import java.util.UUID

import monix.execution.schedulers.TestScheduler
import monix.eval.Task
import monix.execution.Scheduler
import net.kindapipeline.algebras.{DeleteAlgebraImpl, KafkaAlgebra, KafkaDummy}
import net.kindapipeline.model.DeleteRequest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DeleteSpec extends AnyFlatSpec with Matchers {
  behavior of "DeleteAlgebraImpl"
  import DeleteSpec._
  implicit val sc: Scheduler = TestScheduler()

  it should "return correct outcome for correct request" in {
    implicit val kafka = new KafkaDummy[Task]()
    mkAlg.delete(correct).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 204
        println(kafka.published)
        kafka.published.head shouldEqual correct
    }
  }

  it should "return BadRequest outcome for request with unsupported resource " in {
    implicit val kafka = new KafkaDummy[Task]()
    mkAlg.delete(incorrect).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 400
        kafka.published shouldBe empty
    }
  }
}

object DeleteSpec {
  def allowedResources = List("Foo", "Bar")
  def correct: DeleteRequest = DeleteRequest("Bar", UUID.fromString("a4d8bf4e-84ca-40fd-be4f-e83c715e4881"))
  def incorrect: DeleteRequest = DeleteRequest("Kek", UUID.fromString("521622c7-a4fe-4028-a17a-2fd471c56b9d"))
  def mkAlg(implicit da: KafkaAlgebra[Task]) = {
    new DeleteAlgebraImpl[Task](allowedResources)
  }
}
