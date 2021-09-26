package no.perok.toucan.domain.security

import munit._
import no.perok.toucan.infrastructure.CryptographyLogic

class CryptographyLogicSpec extends FunSuite {

  val clearTextPassword = "1234567890"
  val hash: String = CryptographyLogic.hashPassword(clearTextPassword)

  test("The hashed password should be correctly verified") {
    assert(CryptographyLogic.verifyPassword(clearTextPassword, hash), true)
  }

  test("The hashed password not work on incorrect passwords") {
    assertEquals(CryptographyLogic.verifyPassword("notCorrect", hash), false)
  }

  test("The hashed password should have correct length") {
    assertEquals(hash.length, 60)
  }
}
