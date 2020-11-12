package net.kindapipeline.model

import java.util.UUID

sealed trait Request

case class PutRequest(resourceType: String, id: UUID, mediaType: String, body: String) extends Request

case class DeleteRequest(resourceType: String, id: UUID) extends Request
