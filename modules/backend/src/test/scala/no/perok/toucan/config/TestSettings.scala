package no.perok.toucan.config

object TestSettings {
  def apply(): Config =
    Config("", "", Server(0), DB("", "", ""))
}
