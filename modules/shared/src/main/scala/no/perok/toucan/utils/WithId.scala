package no.perok.toucan.domain.models

import cats.{Eq, Show}
import io.circe.*
import io.circe.generic.semiauto.*

import scala.util.Try

final case class WithId[A](id: ID[A], model: A)

// @scala.annotation.nowarn
object WithId:
  given [A: Codec]: Codec[WithId[A]] = deriveCodec

/* We don't need pattern matching, so make it a normal class. But make
 * the `toRaw` member field public so we can convert the ID back to its
 * contained raw type at any time.
 *
 * Concept from: https://users.scala-lang.org/t/generic-implemention-for-common-case-classes/2128/3
 */

final class ID[Tag] private (val toRaw: Int) extends AnyVal
object ID:
  def apply[Tag](id: Int): ID[Tag] =
    new ID(id)

  def apply[Tag](string: String): Try[ID[Tag]] =
    Try(string.toInt).map(new ID(_))

  given [Tag]: Eq[ID[Tag]] = Eq.fromUniversalEquals
  given [Tag]: Show[ID[Tag]] = Show.show(_.toRaw.toString)

  given [Tag]: Encoder[ID[Tag]] = Encoder.encodeInt.contramap(_.toRaw)
  given [Tag]: Decoder[ID[Tag]] = Decoder.decodeInt.map(ID[Tag])
// import org.tpolecat.typename.*

// implicit def phantomIdMeta[T](implicit tt: TypeName[ID[T]]): Meta[ID[T]] =
//   Meta[Int].timap(ID[T])(a => a.toRaw)
