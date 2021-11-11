package no.perok.toucan.infrastructure

import org.mindrot.jbcrypt.BCrypt

/**
  * TODO libsodium with argon2
  */
object CryptographyLogic:
  def hashPassword(pw: String): String =
    BCrypt.hashpw(pw, BCrypt.gensalt())

  def verifyPassword(pw: String, hash: String): Boolean =
    BCrypt.checkpw(pw, hash)
