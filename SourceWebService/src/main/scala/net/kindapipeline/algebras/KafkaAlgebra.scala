package net.kindapipeline.algebras
import cats.Monad
import cats.effect.Sync
import net.kindapipeline.model.{DeleteRequest, PutRequest, Request}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

trait KafkaAlgebra[F[_]] {
  def publishResource(rq: Request): F[Unit]
}

class KafkaAlgebraImpl[F[_]: Sync](kafkaProducer: KafkaProducer[String, String])(topic: String)
  extends KafkaAlgebra[F] {
  private def toRecord = requestToRecord(topic)(_)
  override def publishResource(rq: Request): F[Unit] = {
    Sync[F].delay(kafkaProducer.send(toRecord(rq)))
  }
  private def requestToRecord(topic: String)(rq: Request): ProducerRecord[String, String] = rq match {
    case PutRequest(_, id, _, body) =>
      new ProducerRecord(topic, id.toString, body)
    case DeleteRequest(_, id) =>
      new ProducerRecord(topic, id.toString, "")
  }
}

class KafkaDummy[F[_]: Monad] extends KafkaAlgebra[F] {
  var published = List.empty[Request]
  override def publishResource(rq: Request): F[Unit] = {
    published = published :+ rq
    Monad[F].unit
  }
}
