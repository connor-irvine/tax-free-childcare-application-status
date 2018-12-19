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
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.{AuthProviders, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.taxfreechildcareapplicationstatus.config.TfcasAuthConnector
import uk.gov.hmrc.taxfreechildcareapplicationstatus.controllers.GetTfcHistoryController._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers.GetTfcHistoryParser._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.models.ApiPlatformErrorResponse._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.services.GetTfcHistoryService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTfcHistoryController @Inject()(cc: ControllerComponents,
                                        override val authConnector: TfcasAuthConnector,
                                        getTfcHistoryService: GetTfcHistoryService,
                                        implicit val executionContext: ExecutionContext
                                       ) extends BackendController(cc) with AuthorisedFunctions {

  private def headerMatches(key: String, pattern: String)(implicit request: Request[_]): Boolean =
    request.headers.get(key).exists(_.matches(pattern))

  private def validateRequest(nino: String, uniqueClaimsId: String)(action: => Future[Result])(implicit request: Request[_]): Future[Result] =
    (nino.matches(ninoRegex),
      uniqueClaimsId.matches(ucidRegex),
      headerMatches(originatorId, originatorRegex),
      headerMatches(correlationId, correlationIdRegex)
    ) match {
      case (true, true, true, true) => action
      case (false, _, _, _) => Future.successful(BadRequest(Json.toJson(InvalidNinoResponse)))
      case (_, false, _, _) => Future.successful(BadRequest(Json.toJson(InvalidUcidResponse)))
      case (_, _, false, _) => Future.successful(BadRequest(Json.toJson(InvalidOriginatorIdResponse)))
      case (_, _, _, false) => Future.successful(BadRequest) // TODO confirm error json once it's defined
    }

  def getTfcHistory(nino: String, uniqueClaimsId: String): Action[AnyContent] =
    Action.async { implicit request =>
      val updatedHc = implicitly[HeaderCarrier].withExtraHeaders(
        Map[String, Option[String]](
          originatorId -> request.headers.get(originatorId),
          correlationId -> request.headers.get(correlationId)
        ).collect {
          case (k, Some(v)) => (k, v)
        }.toSeq: _*
      )
      authorised(AuthProviders(PrivilegedApplication)) {
        validateRequest(nino, uniqueClaimsId) {
          {
            getTfcHistoryService.getClaimsHistory(nino, uniqueClaimsId)(updatedHc) map {
              case Right(json) => Ok(json)
              case Left(InvalidNinoErr) => BadRequest(Json.toJson(InvalidNinoResponse))
              case Left(InvalidUcidErr) => BadRequest(Json.toJson(InvalidUcidResponse))
              case Left(InvalidOriginatorIdErr) => BadRequest(Json.toJson(InvalidOriginatorIdResponse))
              case Left(BusinessValidationErr(message)) => BadRequest(Json.toJson(BusinessValidationResponse(message)))
              case Left(NotFoundErr(message)) => NotFound(Json.toJson(NotFoundResponse(message)))
              case Left(ServerErrorErr) => InternalServerError(Json.toJson(InternalServerErrorResponse))
              case Left(ServiceUnavailableErr) => ServiceUnavailable(Json.toJson(ServiceUnavailableResponse))
              case Left(GetTfcHistoryUnexpectedError(status, _)) if status == OK =>
                // todo logging? n.b. if we do log then we must not log the body as it may contain sensitive info
                InternalServerError(Json.toJson(UnexpectedServerErrorResponse))
              case Left(GetTfcHistoryUnexpectedError(status, body)) =>
                // todo logging?
                InternalServerError(Json.toJson(UnexpectedServerErrorResponse))
            }
          } recover {
            case _ =>
              // todo logging?
              InternalServerError(Json.toJson(UnexpectedServerErrorResponse))
          }
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

}
