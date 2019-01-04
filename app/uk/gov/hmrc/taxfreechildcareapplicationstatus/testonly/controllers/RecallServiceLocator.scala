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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.testonly.controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.taxfreechildcareapplicationstatus.connectors.TfcasServiceLocator

import scala.concurrent.ExecutionContext


@Singleton
class RecallServiceLocator @Inject()(serviceLocator: TfcasServiceLocator,
                                     cc: ControllerComponents,
                                     implicit val executionContext: ExecutionContext
                                    ) extends BackendController(cc) {

  def recall: Action[AnyContent] = Action.async { implicit request =>
    serviceLocator.register.map { success =>
      Ok(Json.obj("success" -> success))
    }
  }

}
