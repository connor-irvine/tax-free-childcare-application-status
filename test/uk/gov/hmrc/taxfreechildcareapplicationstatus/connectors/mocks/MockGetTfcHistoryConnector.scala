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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.taxfreechildcareapplicationstatus.connectors.GetTfcHistoryConnector
import uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers.GetTfcHistoryParser.GetTfcHistoryResponse

import scala.concurrent.Future

trait MockGetTfcHistoryConnector extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockGetTfcHistoryConnector)
  }

  val mockGetTfcHistoryConnector: GetTfcHistoryConnector = mock[GetTfcHistoryConnector]

  def stubGetClaimsHistory(nino: String,
                           uniqueClaimId: String
                          )(response: Future[GetTfcHistoryResponse]): Unit = {
    when(mockGetTfcHistoryConnector.getClaimsHistory
    (ArgumentMatchers.eq(nino),
      ArgumentMatchers.eq(uniqueClaimId)
    )(ArgumentMatchers.any())).thenReturn(response)
  }

}
