package no.perok.toucan.domain.models

import java.time.LocalDateTime

final case class User(email: String, username: String, created: LocalDateTime)
