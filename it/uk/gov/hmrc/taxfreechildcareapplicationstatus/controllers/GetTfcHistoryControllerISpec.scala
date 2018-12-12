package uk.gov.hmrc.taxfreechildcareapplicationstatus.controllers

import org.scalatest.{Matchers, WordSpec}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.IntegrationTestConstants._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.servicemocks.DesGetTfcHistoryMock._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.{ComponentSpecBase, CustomMatchers}

import scala.io.Source
import GetTfcHistoryController._

class GetTfcHistoryControllerISpec extends WordSpec with Matchers with ComponentSpecBase with CustomMatchers {

  implicit val hc = HeaderCarrier()

  lazy val testOkResponse: JsValue = Json.parse(Source.fromFile("resources/public/api/conf/1.0/examples/200.json").getLines.mkString)
  lazy val test404Response: JsValue = Json.parse(Source.fromFile("resources/public/api/conf/1.0/examples/404.json").getLines.mkString)

  "GET /claims/:nino/:uniqueClaimsId" when {
    "DES returns OK and with a valid response" should {
      "return OK" in {
        stubGetTfcHistory(testNino, testUniqueClaimId)(OK, testOkResponse)

        val result = get(
          uri = s"/claims/$testNino/$testUniqueClaimId",
          headers = Map(
            originatorId -> testOriginatorId,
            correlationId -> testCorrelationId
          )
        )

        result should have(
          httpStatus(OK),
          jsonBodyAs(testOkResponse)
        )
      }
    }
    "DES returns NOT_FOUND and with a valid response" should {
      "return NOT_FOUND" in {
        stubGetTfcHistory(testNino, testUniqueClaimId)(NOT_FOUND, test404Response)

        val result = get(
          uri = s"/claims/$testNino/$testUniqueClaimId",
          headers = Map(
            originatorId -> testOriginatorId,
            correlationId -> testCorrelationId
          )
        )

        result should have(
          httpStatus(NOT_FOUND),
          jsonBodyAs(test404Response)
        )
      }
    }
  }

}
