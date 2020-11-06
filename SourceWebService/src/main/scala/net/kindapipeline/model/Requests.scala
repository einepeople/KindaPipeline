package net.kindapipeline.model

import java.util.UUID

import org.apache.kafka.clients.producer.ProducerRecord

sealed trait Request {
  def toKafkaRecord(topic: String): ProducerRecord[String, String]
}

case class PutRequest(resourceType: String, id: UUID, mediaType: String, body: String) extends Request {
  override def toKafkaRecord(topic: String): ProducerRecord[String, String] = {
    new ProducerRecord(topic, id.toString, body)
  }
}

case class DeleteRequest(resourceType: String, id: UUID) extends Request {
  override def toKafkaRecord(topic: String): ProducerRecord[String, String] = new ProducerRecord(topic, id.toString, "")
}
