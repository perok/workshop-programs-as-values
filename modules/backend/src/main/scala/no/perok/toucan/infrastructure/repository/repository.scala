package no.perok.toucan.infrastructure

import java.time.LocalDateTime

import doobie._
import doobie.implicits.javasql._

package object repository {
  implicit val LocalTimeMeta: Meta[LocalDateTime] =
    Meta[java.sql.Timestamp].imap(ts => ts.toLocalDateTime)(
      dt => java.sql.Timestamp.valueOf(dt)
    )

}
