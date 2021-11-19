package backend.infrastructure.repository

import shared.models.backend.Name
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*

object HelloRepository:
  def getHello =
    sql"""select name from hello_to
         fetch first 1 rows only
      """.query(varchar(80)).gmap[Name]
