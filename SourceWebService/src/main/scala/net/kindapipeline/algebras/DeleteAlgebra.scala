package net.kindapipeline.algebras

import cats.Monad, cats.implicits._
import net.kindapipeline.model.{DeleteRequest, ERROR, INFO, OperationOutcome}

trait DeleteAlgebra[F[_]] {
  def delete(rq: DeleteRequest): F[OperationOutcome]
}

class DeleteAlgebraImpl[F[_]](allowedResources: List[String])(implicit M: Monad[F], K: KafkaAlgebra[F])
  extends DeleteAlgebra[F] {
  override def delete(rq: DeleteRequest): F[OperationOutcome] =
    if (allowedResources contains rq.resourceType)
      K.publishResource(rq) *> M.pure(OperationOutcome(204, INFO, "Successful"))
    else M.pure(OperationOutcome(400, ERROR, "Resource type in the URL is not a valid FHIR Resource"))
}
