package no.perok.toucan.config

import com.comcast.ip4s.Port

object TestSettings {
  def apply(): Config =
    Config("", "", Server(Port.fromInt(1).get), DB("", "", ""))
}
