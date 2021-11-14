package no.perok.toucan.infrastructure.repository

import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import no.perok.toucan.shared.models.backend.*

object HelloRepository:
  def getHello =
    sql"""select name from hello_to
         fetch first 1 rows only
      """.query(varchar(80)).gmap[Name]
