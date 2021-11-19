package no.perok.toucan.config

import com.comcast.ip4s.*

object TestSettings {
  def apply(): Config =
    Config("", "", Server(port"1"), DB("", "", ""))
}
