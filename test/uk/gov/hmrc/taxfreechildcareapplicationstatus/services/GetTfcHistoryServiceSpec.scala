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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.services

import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.taxfreechildcareapplicationstatus.connectors.mocks.MockGetTfcHistoryConnector
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.TestConstants._

import scala.concurrent.Future

class GetTfcHistoryServiceSpec extends WordSpecLike with Matchers with MockGetTfcHistoryConnector {

  object TestGetTfcHistoryService extends GetTfcHistoryService(mockGetTfcHistoryConnector)

  implicit val hc = HeaderCarrier()

  "getClaimsHistory" should {
    "return what the connector returns" in {
      val testResponse = Json.obj()

      val connectorResult = Future.successful(Right(testResponse))

      stubGetClaimsHistory(testNino, testUniqueClaimId)(connectorResult)

      val serviceResult = TestGetTfcHistoryService.getClaimsHistory(testNino, testUniqueClaimId)

      serviceResult shouldBe connectorResult
    }
  }

}
