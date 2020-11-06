package net.kindapipeline.algebras

import java.util.UUID

import cats.Monad
import sttp.tapir.json.circe._
import net.kindapipeline.errors._
import net.kindapipeline.model._
import tofu._
import tofu.syntax.monadic._
import io.circe.parser._
import io.circe.Json
import net.kindapipeline.model.Severity._

trait PutAlgebra[F[_]] {
  def putResource(rq: PutRequest): F[OperationOutcome]
}

class PutAlgebraImpl[F[_]](allowedResources: List[String])(allowedMediaTypes: List[String])(
  implicit M: Monad[F],
  R: Raise[F, WebError],
  H: Handle[F, WebError],
  K: KafkaAlgebra[F]
) extends PutAlgebra[F] {

  override def putResource(rq: PutRequest): F[OperationOutcome] = {
    // Move checking from publishing?
    val result = for {
      _ <- checkInList(rq.resourceType, allowedResources)(MalformedUrlResource)
      _ <- checkInList(rq.mediaType, allowedMediaTypes)(UnsupportedMediaType)
      json <- payloadToJSON(rq)
      _ <- checkJsonField(json, "resourceType", rq.resourceType)(NonMatchingResource)
      _ <- checkJsonField(json, "id", rq.id.toString)(NonMatchingId)
      _ <- K.publishResource(rq)
    } yield OperationOutcome(200, INFO, "Successful")

    H.handle(result)(handleToOutcome)
  }

  def checkInList(value: String, allowed: List[String])(onNotContain: WebError): F[Unit] = {
    if (allowed contains value) Monad[F].unit
    else R.raise(onNotContain)
  }

  def payloadToJSON(rq: PutRequest): F[Json] =
    parse(rq.body) match {
      case Left(err) =>
        println(err) //debug
        R.raise(UnprocessableBody)
      case Right(json: Json) => M.pure(json)
    }

  def checkJsonField(json: Json, fieldName: String, expected: String)(onError: WebError): F[Unit] = {
    json.hcursor
      .downField(fieldName)
      .as[String]
      .toOption
      .filter(_ equals expected) match {
      case Some(_) => M.unit
      case None => R.raise(onError)
    }
  }

  def handleToOutcome(err: WebError): OperationOutcome = err match {
//    case MalformedUrlId =>
//      OperationOutcome(400, ERROR, "Resource ID in the URL is not a valid GUID")
    case MalformedUrlResource =>
      OperationOutcome(400, ERROR, "Resource type in the URL is not a valid FHIR Resource")
    case UnsupportedMediaType =>
      OperationOutcome(415, ERROR, "This Content-Type is unsupported")
    case UnprocessableBody =>
      OperationOutcome(422, ERROR, "Request payload cannot be parsed as a valid JSON")
    case NonMatchingId =>
      OperationOutcome(422, ERROR, "Resource ID in the payload does not exist or match one in the URL")
    case NonMatchingResource =>
      OperationOutcome(422, ERROR, "Resource type in the payload does not exist or match one in the URL")
  }

}
