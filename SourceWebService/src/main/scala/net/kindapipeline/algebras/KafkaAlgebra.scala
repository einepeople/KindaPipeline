package net.kindapipeline.algebras
import cats.Monad
import cats.effect.Sync
import monix.eval.Task
import cats.syntax.option
import net.kindapipeline.model.Request
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

trait KafkaAlgebra[F[_]] {
  def publishResource(rq: Request): F[Unit]
}

class KafkaAlgebraImpl[F[_]: Sync](kafkaProducer: KafkaProducer[String, String])(topic: String)
  extends KafkaAlgebra[F] {
  override def publishResource(rq: Request): F[Unit] = {
    Sync[F].delay(kafkaProducer.send(rq.toKafkaRecord(topic)))
  }
}
