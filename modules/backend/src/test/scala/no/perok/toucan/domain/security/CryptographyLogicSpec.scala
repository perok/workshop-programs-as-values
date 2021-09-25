package no.perok.toucan.domain.security

import no.perok.toucan.infrastructure.CryptographyLogic
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CryptographyLogicSpec extends AnyFlatSpec with Matchers {

  val clearTextPassword = "1234567890"
  val hash: String = CryptographyLogic.hashPassword(clearTextPassword)

  "The hashed password" should "be correctly verified" in {
    CryptographyLogic.verifyPassword(clearTextPassword, hash) shouldEqual true
  }

  "The hashed password" should "not work on incorrect passwords" in {
    CryptographyLogic.verifyPassword("notCorrect", hash) shouldEqual false
  }

  "The hashed password" should "have correct length" in {
    hash.length shouldEqual 60
  }
}
