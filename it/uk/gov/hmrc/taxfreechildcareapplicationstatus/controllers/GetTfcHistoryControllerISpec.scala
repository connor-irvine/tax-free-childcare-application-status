/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.controllers

import org.scalatest.{Matchers, WordSpec}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.taxfreechildcareapplicationstatus.TfcasConstants.InternalServerErrorAPIHeader
import uk.gov.hmrc.taxfreechildcareapplicationstatus.controllers.GetTfcHistoryController._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.IntegrationTestConstants._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.servicemocks.DesGetTfcHistoryStub._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.{ComponentSpecBase, CustomMatchers}

import scala.io.Source

class GetTfcHistoryControllerISpec extends WordSpec with Matchers with ComponentSpecBase with CustomMatchers {

  implicit val hc = HeaderCarrier()

  lazy val testOkResponse: JsValue = Json.parse(Source.fromFile("public/api/conf/1.0/examples/200-success.json").getLines.mkString)
  lazy val test404DesResponse: JsValue = Json.parse(Source.fromFile("test/resources/des/404-not-found.json").getLines.mkString)
  lazy val test404ApiResponse: JsValue = Json.parse(Source.fromFile("public/api/conf/1.0/examples/404-not-found.json").getLines.mkString)

  "GET /claims/:nino/:uniqueClaimsId" when {
    "AUTH returns UNAUTHORIZED " should {
      "return UNAUTHORIZED" in {
        stubUnauthorised()

        val result = get(
          uri = s"/claims/$testNino/$testUniqueClaimId",
          headers = Map(
            originatorId -> testOriginatorId,
            correlationId -> testCorrelationId
          )
        )

        result should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }

    "DES returns OK and with a valid response" should {
      "return OK" in {
        stubAuthorised()
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
        stubAuthorised()
        stubGetTfcHistory(testNino, testUniqueClaimId)(NOT_FOUND, test404DesResponse)

        val result = get(
          uri = s"/claims/$testNino/$testUniqueClaimId",
          headers = Map(
            originatorId -> testOriginatorId,
            correlationId -> testCorrelationId
          )
        )

        result should have(
          httpStatus(NOT_FOUND),
          jsonBodyAs(test404ApiResponse)
        )
      }
    }

    "DES returns an invalid response" should {
      "return INTERNAL_SERVER_ERROR" in {
        stubAuthorised()
        stubGetTfcHistory(testNino, testUniqueClaimId)(NOT_FOUND, "")

        val result = get(
          uri = s"/claims/$testNino/$testUniqueClaimId",
          headers = Map(
            originatorId -> testOriginatorId,
            correlationId -> testCorrelationId
          )
        )

        result should have(
          httpStatus(INTERNAL_SERVER_ERROR),
          jsonBodyAs(Json.obj(
            "code" -> InternalServerErrorAPIHeader,
            "message" -> "Something unexpected went wrong"
          ))
        )
      }
    }
  }

}
