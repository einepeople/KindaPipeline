package net.kindapipeline.model

import io.circe.Encoder

sealed trait Severity
case object INFO extends Severity {
  override def toString: String = "info"
}
case object WARN extends Severity {
  override def toString: String = "warn"
}
case object ERROR extends Severity {
  override def toString: String = "error"
}
case object FATAL extends Severity {
  override def toString: String = "fatal"
}

object Severity {
  implicit def decoder(implicit t: Encoder[String]): Encoder[Severity] = (a: Severity) => t(a.toString)
}
