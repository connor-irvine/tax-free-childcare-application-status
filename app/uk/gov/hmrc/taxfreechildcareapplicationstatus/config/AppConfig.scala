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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.config

import java.io.FileNotFoundException
import javax.inject.{Inject, Singleton}

import play.api.Environment
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(configuration: ServicesConfig,
                          env: Environment) {

  import configuration._

  private def getJson(file: String): JsValue =
    Json.parse(env.resourceAsStream(file).getOrElse(throw new FileNotFoundException(s"cannot find: $file")))

  private def getCriticalConfig(key:String) =
    getConfString(key, throw new InternalServerException(s"Cannot find critical config for: $key"))

  def authUrl: String = baseUrl("auth")

  def getDesUrl: String = baseUrl("des")

  lazy val getTfcHistoryOkResponseSchema: JsValue = getJson("/resources/schemas/GetTfcHistorySuccess.json")

  lazy val getTfcHistoryFailureResponseSchema: JsValue = getJson("/resources/schemas/GetTfcHistoryFailure.json")

  lazy val desEnvironmentHeader :String = getCriticalConfig("des.environment")

  lazy val desAuthorisationToken :String = getCriticalConfig("des.authorisation-token")

}
