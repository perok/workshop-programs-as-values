package backend.config

import com.comcast.ip4s.*

object TestSettings {
  def apply(): Config =
    Config("", "", DB("", "", ""))
}
