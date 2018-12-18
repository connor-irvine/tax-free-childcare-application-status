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

import javax.inject.{Inject, Singleton}

import controllers.Assets
import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.taxfreechildcareapplicationstatus.config.AppConfig
import uk.gov.hmrc.taxfreechildcareapplicationstatus.views.txt

@Singleton
class DocumentationController @Inject()(appConfig: AppConfig,
                                        cc: ControllerComponents,
                                        assets: Assets,
                                        errorHandler: HttpErrorHandler)
  extends uk.gov.hmrc.api.controllers.DocumentationController(cc, assets, errorHandler) {

  override def conf(version: String, endpointName: String): Action[AnyContent] =
   assets.at(s"/public/api/conf/$version", endpointName)

  override def definition(): Action[AnyContent] = Action {
    Ok(txt.definition(appConfig.apiAccess, appConfig.apiContext))
  }

}
