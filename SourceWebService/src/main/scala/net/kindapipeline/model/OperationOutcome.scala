package net.kindapipeline.model

import sttp.tapir.json.circe._

case class OperationOutcome(status: Int, severity: Severity, details: String)
