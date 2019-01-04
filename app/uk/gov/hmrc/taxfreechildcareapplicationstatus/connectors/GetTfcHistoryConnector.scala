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

import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.taxfreechildcareapplicationstatus.config.AppConfig
import uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers.GetTfcHistoryParser
import uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers.GetTfcHistoryParser._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTfcHistoryConnector @Inject()(appConfig: AppConfig,
                                       httpClient: HttpClient,
                                       parser: GetTfcHistoryParser,
                                       implicit val ec: ExecutionContext) {

  import parser._

  private def url(nino: String, uniqueClaimId: String) =
    s"${appConfig.getDesUrl}/tax-free-childcare/claims/$nino/$uniqueClaimId"

  def getClaimsHistory(nino: String, uniqueClaimId: String)(implicit hc: HeaderCarrier): Future[GetTfcHistoryResponse] = {
    val headerCarrier = hc
      .withExtraHeaders(
        "Environment" -> appConfig.desEnvironmentHeader,
        "Authorization" -> s"Bearer ${appConfig.desAuthorisationToken}"
      )
    httpClient.GET[GetTfcHistoryResponse](url(nino, uniqueClaimId))(
      implicitly,
      headerCarrier,
      implicitly
    )
  }

}
