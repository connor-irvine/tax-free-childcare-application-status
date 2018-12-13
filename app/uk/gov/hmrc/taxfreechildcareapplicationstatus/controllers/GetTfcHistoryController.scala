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

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.taxfreechildcareapplicationstatus.controllers.GetTfcHistoryController._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers.GetTfcHistoryParser._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.services.GetTfcHistoryService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTfcHistoryController @Inject()(cc: ControllerComponents,
                                        getTfcHistoryService: GetTfcHistoryService,
                                        implicit val executionContext: ExecutionContext
                                       ) extends BackendController(cc) {

  private def headerMatches(key: String, pattern: String)(implicit request: Request[_]): Boolean =
    request.headers.get(key).exists(_.matches(pattern))

  private def validateRequest(nino: String, uniqueClaimsId: String)(action: => Future[Result])(implicit request: Request[_]): Future[Result] =
    (nino.matches(ninoRegex),
      uniqueClaimsId.matches(ucidRegex),
      headerMatches(originatorId, originatorRegex),
      headerMatches(correlationId, correlationIdRegex)
    ) match {
      case (true, true, true, true) => action
      case (false, _, _, _) => Future.successful(BadRequest(Json.toJson(InvalidNinoErr)))
      case (_, false, _, _) => Future.successful(BadRequest(Json.toJson(InvalidUcidErr)))
      case (_, _, false, _) => Future.successful(BadRequest(Json.toJson(InvalidOriginatorIdErr)))
      case (_, _, _, false) => Future.successful(BadRequest) // TODO confirm error json once it's defined
    }

  def getTfcHistory(nino: String, uniqueClaimsId: String): Action[AnyContent] =
    Action.async { implicit request =>
      validateRequest(nino, uniqueClaimsId) {
        {
          getTfcHistoryService.getClaimsHistory(nino, uniqueClaimsId) map {
            case Right(json) => Ok(json)
            case Left(x@(InvalidNinoErr | InvalidUcidErr | InvalidOriginatorIdErr)) => BadRequest(Json.toJson(x.asInstanceOf[GetTfcHistoryError]))
            case Left(x@GetTfcHistoryError(NotFoundErrCode, _)) => NotFound(Json.toJson(x))
            case Left(x@GetTfcHistoryError(BusinessValidationErrCode, _)) => BadRequest(Json.toJson(x))
            case Left(ServerErrorErr) => InternalServerError(Json.toJson(ServerErrorErr))
            case Left(ServiceUnavailableErr) => ServiceUnavailable(Json.toJson(ServiceUnavailableErr))
            case Left(GetTfcHistoryUnexpectedError(status, _)) if status == OK =>
              // todo logging?
              throw UnexpectedException
            case Left(GetTfcHistoryUnexpectedError(status, body)) =>
              // todo logging?
              throw UnexpectedException
          }
        } recover {
          case _ =>
            // todo logging?
            throw UnexpectedException
        }
      }
    }

}

object GetTfcHistoryController {

  val originatorId = "Originator-Id"
  val correlationId = "CorrelationId"

  val ninoRegex = "^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D]$"
  val ucidRegex = "^\\d{11}$"
  val originatorRegex = "^(.*)$"
  val correlationIdRegex = "^[A-Za-z0-9\\-]{36}$"

  val UnexpectedException = new InternalServerException("Something unexpected went wrong")

}
