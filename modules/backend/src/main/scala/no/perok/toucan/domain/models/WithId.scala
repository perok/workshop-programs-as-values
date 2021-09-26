package no.perok.toucan.domain.models

import cats.{Eq, Show}
import io.circe._
import io.circe.derivation._
import doobie._
import scala.reflect.runtime.universe.TypeTag

import scala.util.Try

final case class WithId[A](id: ID[A], model: A)
object WithId {
  implicit def jsonEncoderInstance[A: Encoder]: Encoder[WithId[A]] = deriveEncoder
  implicit def jsonDecoderInstance[A: Decoder]: Decoder[WithId[A]] = deriveDecoder
}

/* We don't need pattern matching, so make it a normal class. But make
 * the `toRaw` member field public so we can convert the ID back to its
 * contained raw type at any time.
 *
 * Concept from: https://users.scala-lang.org/t/generic-implemention-for-common-case-classes/2128/3
 */

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class ID[Tag] private (val toRaw: Int) extends AnyVal
object ID {
  def apply[Tag](id: Int): ID[Tag] =
    new ID(id)

  def apply[Tag](string: String): Try[ID[Tag]] =
    Try(string.toInt).map(new ID(_))

  implicit def eqInstance[Tag]: Eq[ID[Tag]] = Eq.fromUniversalEquals
  implicit def showInstance[Tag]: Show[ID[Tag]] = Show.show(_.toRaw.toString)

  implicit def jsonEncoderInstance[Tag]: Encoder[ID[Tag]] = Encoder.encodeInt.contramap(_.toRaw)
  implicit def jsonDecoderInstance[Tag]: Decoder[ID[Tag]] = Decoder.decodeInt.map(ID[Tag])

  implicit def phantomIdMeta[T](implicit tt: TypeTag[ID[T]]): Meta[ID[T]] =
    Meta[Int].timap(ID[T])(a => a.toRaw)
}