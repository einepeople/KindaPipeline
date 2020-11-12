package net.kindapipeline.specs.unit

import java.util.UUID

import monix.execution.schedulers.TestScheduler
import monix.eval.Task
import monix.execution.Scheduler
import net.kindapipeline.algebras.{DeleteAlgebraImpl, KafkaDummy}
import net.kindapipeline.model.DeleteRequest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DeleteSpec extends AnyFlatSpec with Matchers {
  behavior of "DeleteAlgebraImpl"
  import DeleteSpec._
  it should "return correct outcome for correct request" in {
    implicit val kafka: KafkaDummy[Task] = new KafkaDummy[Task]()
    val delAlg = new DeleteAlgebraImpl[Task](allowedResources)
    delAlg.delete(correct).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 204
        implicitly[KafkaDummy[Task]].published.head shouldEqual correct
    }
  }

  it should "return BadRequest outcome for incorrect request" in {
    implicit val kafka: KafkaDummy[Task] = new KafkaDummy[Task]()
    val delAlg = new DeleteAlgebraImpl[Task](allowedResources)
    delAlg.delete(incorrect).runAsync {
      case Left(_) => fail()
      case Right(value) =>
        value.status shouldEqual 400
        implicitly[KafkaDummy[Task]].published shouldBe empty
    }
  }
}

object DeleteSpec {
  implicit val sc: Scheduler = TestScheduler()
  val allowedResources = List("Foo", "Bar")
  val correct: DeleteRequest = DeleteRequest("Bar", UUID.randomUUID())
  val incorrect: DeleteRequest = DeleteRequest("Kek", UUID.randomUUID())
}
