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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.Application
import play.api.libs.json.Writes
import uk.gov.hmrc.taxfreechildcareapplicationstatus.config.AppConfig
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.WiremockMethods


object DesGetTfcHistoryStub extends WiremockMethods {

  def url(nino: String, id: String) = s"/tax-free-childcare/claims/$nino/$id"

  def stubGetTfcHistory[T](nino: String, id: String)(status: Int, body: T)(implicit writes: Writes[T], app: Application): StubMapping = {
    val appConfig = app.injector.instanceOf[AppConfig]
    when(method = GET, uri = url(nino, id),
      headers = Map(
        "Environment" -> appConfig.desEnvironmentHeader,
        "Authorization" -> s"Bearer ${appConfig.desAuthorisationToken}"
      )
    ).thenReturn(status = status, body = writes.writes(body))
  }

}