package backend.infrastructure.repository

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

import shared.models.backend.Name

object HelloRepository:
  def getHello =
    sql"""select name from hello_to
         fetch first 1 rows only
      """.query(varchar(80)).gmap[Name]
