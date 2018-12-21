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

import play.api.{Configuration, Environment}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.taxfreechildcareapplicationstatus.models.api.APIAccess

@Singleton
class AppConfig @Inject()(configuration: Configuration,
                          serviceConfig: ServicesConfig,
                          env: Environment) {

  import serviceConfig._

  lazy val appNameForServiceLocator: String = getString("appNameForServiceLocator")

  lazy val appUrl: String = getString("appUrl")

  private def getJson(file: String): JsValue =
    Json.parse(env.resourceAsStream(file).getOrElse(throw new FileNotFoundException(s"cannot find: $file")))

  private def getCriticalConfig(key: String) =
    getConfString(key, throw new InternalServerException(s"Cannot find critical config for: $key"))

  lazy val authUrl: String = baseUrl("auth")

  lazy val getDesUrl: String = baseUrl("des")

  lazy val serviceLocatorUrl: String = baseUrl("service-locator")

  lazy val getTfcHistoryOkResponseSchema: JsValue = getJson("/resources/schemas/success-response-schema.json")

  lazy val getTfcHistoryFailureResponseSchema: JsValue = getJson("/resources/schemas/error-response-schema.json")

  lazy val desEnvironmentHeader: String = getCriticalConfig("des.environment")

  lazy val desAuthorisationToken: String = getCriticalConfig("des.authorisation-token")

  private val apiContextConfigKey = "api.context"
  private val apiAccessTypeKey = "api.access.type"
  private val apiAccessWhitelistKey = "api.access.whitelistedApplicationIds"

  lazy val apiContext: String = serviceConfig.getString(apiContextConfigKey)
  lazy val apiAccess: APIAccess = APIAccess(serviceConfig.getString(apiAccessTypeKey),
    configuration.get[Option[Seq[String]]](apiAccessWhitelistKey))

}
