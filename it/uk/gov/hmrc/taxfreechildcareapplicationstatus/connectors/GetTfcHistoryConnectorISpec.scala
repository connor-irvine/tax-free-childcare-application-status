/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.taxfreechildcareapplicationstatus.connectors

import org.scalatest.{Matchers, WordSpec}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.taxfreechildcareapplicationstatus.controllers.GetTfcHistoryController.{correlationId, originatorId}
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.ComponentSpecBase
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.IntegrationTestConstants._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.servicemocks.DesGetTfcHistoryStub._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers.GetTfcHistoryParser._

import scala.io.Source

class GetTfcHistoryConnectorISpec extends WordSpec with Matchers with ComponentSpecBase {

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders(
    originatorId -> testOriginatorId,
    correlationId -> testCorrelationId
  )

  lazy val connector: GetTfcHistoryConnector = app.injector.instanceOf[GetTfcHistoryConnector]

  lazy val testOkDesResponse: JsValue = Json.parse(Source.fromFile("test/resources/des/200-success.json").getLines.mkString)
  lazy val test404DesResponse: JsValue = Json.parse(Source.fromFile("test/resources/des/404-not-found.json").getLines.mkString)


  "GetTfcHistoryConnector" when {
    "Des returns OK" should {
      "return the json" in {
        stubGetTfcHistory(testNino, testUniqueClaimId)(OK, testOkDesResponse)

        val response = connector.getClaimsHistory(testNino, testUniqueClaimId).futureValue

        response shouldBe Right(testOkDesResponse)
      }
    }

    "Des returns NOT_FOUND" should {
      "return error message" in {
        stubGetTfcHistory(testNino, testUniqueClaimId)(NOT_FOUND, test404DesResponse)

        val response = connector.getClaimsHistory(testNino, testUniqueClaimId).futureValue

        response shouldBe Left(NotFoundErr("The back end has returned a not found response: *backend reason for not found*"))
      }
    }
  }

}
