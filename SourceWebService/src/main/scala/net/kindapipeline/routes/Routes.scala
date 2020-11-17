package net.kindapipeline.routes

import java.util.UUID

import sttp.tapir._
import sttp.model.StatusCode.NoContent
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import net.kindapipeline.model.{DeleteRequest, OperationOutcome, PutRequest}

object Routes {

  private val baseEndpoint =
    endpoint
      .in(path[String]("resourceType") / path[UUID]("id"))
      .out(jsonBody[OperationOutcome])

  val putEndpoint =
    baseEndpoint.put
      .in(header[String]("Content-type"))
      .in(stringBody)
      .mapInTo(PutRequest)
      .out(statusCode)

  val deleteEndpoint = baseEndpoint.delete
    .mapInTo(DeleteRequest)
    .out(statusCode)
}
