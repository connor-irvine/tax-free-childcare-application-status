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

import play.api.Logger
import uk.gov.hmrc.api.domain.Registration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.taxfreechildcareapplicationstatus.config.AppConfig

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class TfcasServiceLocator @Inject()(appConfig: AppConfig,
                                    http: HttpClient,
                                    implicit val executionContext: ExecutionContext) {

  val metadata: Option[Map[String, String]] = Some(Map("third-party-api" -> "true"))

  def register(implicit hc: HeaderCarrier): Future[Boolean] = {
    val registration = Registration(appConfig.appNameForServiceLocator, appConfig.appUrl, metadata)
    http.POST(s"${appConfig.serviceLocatorUrl}/registration", registration, Seq("Content-Type" -> "application/json")) map {
      _ =>
        Logger.info("Service is registered on the service locator")
        true
    } recover {
      case e: Throwable =>
        Logger.error(s"Service could not register on the service locator", e)
        false
    }
  }

}
