package uk.gov.hmrc.taxfreechildcareapplicationstatus.controllers

import org.scalatest.{Matchers, WordSpec}
import play.api.http.Status._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.{ComponentSpecBase, CustomMatchers}

class MicroserviceHelloWorldISpec extends WordSpec with Matchers with ComponentSpecBase with CustomMatchers {

  "GET /hello-world" should {
    "return a status of OK" in {

      val result = get("/hello-world")

      result should have(
        httpStatus(OK)
      )
    }
  }

}
