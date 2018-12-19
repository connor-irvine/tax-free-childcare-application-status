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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers

import javax.inject.{Inject, Singleton}

import com.eclipsesource.schema._
import play.api.http.Status._
import play.api.libs.json.{JsSuccess, JsValue, Json, OFormat}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.taxfreechildcareapplicationstatus.TfcasConstants._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.config.AppConfig

@Singleton
class GetTfcHistoryParser @Inject()(appConfig: AppConfig) {

  private def useAsSchema(jsValue: JsValue): SchemaType = Json.fromJson[SchemaType](jsValue).get

  lazy val okSchema: SchemaType = useAsSchema(appConfig.getTfcHistoryOkResponseSchema)

  lazy val failureSchema: SchemaType = useAsSchema(appConfig.getTfcHistoryFailureResponseSchema)

  lazy val validator = SchemaValidator()

  import GetTfcHistoryParser._

  implicit object GetTfcHistoryRequestHttpReads extends HttpReads[GetTfcHistoryResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetTfcHistoryResponse = {
      lazy val jsonBody = response.json
      lazy val isValidOkResponse = validator.validate(okSchema, jsonBody).isSuccess
      lazy val isValidFailureResponse = validator.validate(failureSchema, jsonBody).isSuccess
      response.status match {
        case OK if isValidOkResponse =>
          Right(jsonBody)
        case BAD_REQUEST if isValidFailureResponse =>
          Left(jsonBody.validate[GetTfcHistoryError] match {
            case JsSuccess(GetTfcHistoryError(InvalidNinoHeader, InvalidNinoMessage), _) => InvalidNinoErr
            case JsSuccess(GetTfcHistoryError(InvalidUcidHeader, InvalidUcidMessage), _) => InvalidUcidErr
            case JsSuccess(GetTfcHistoryError(InvalidOriginatorIdHeader, InvalidOriginatorIdMessage), _) => InvalidOriginatorIdErr
            case JsSuccess(GetTfcHistoryError(BusinessValidationHeader, errMsg), _) if errMsg.matches(BusinessValidationErrMessageRegex) => BusinessValidationErr(errMsg)
            case _ => GetTfcHistoryUnexpectedError(BAD_REQUEST, response.body)
          })
        case NOT_FOUND if isValidFailureResponse =>
          Left(jsonBody.validate[GetTfcHistoryError] match {
            case JsSuccess(response@GetTfcHistoryError(NotFoundHeader, errMsg), _) if errMsg.matches(NotFoundErrMessageRegex) => NotFoundErr(errMsg)
            case _ => GetTfcHistoryUnexpectedError(NOT_FOUND, response.body)
          })
        case INTERNAL_SERVER_ERROR if isValidFailureResponse =>
          Left(jsonBody.validate[GetTfcHistoryError] match {
            case JsSuccess(GetTfcHistoryError(InternalServerErrorDESHeader, InternalServerErrorMessage), _) => ServerErrorErr
            case _ => GetTfcHistoryUnexpectedError(INTERNAL_SERVER_ERROR, response.body)
          })
        case SERVICE_UNAVAILABLE if isValidFailureResponse =>
          Left(jsonBody.validate[GetTfcHistoryError] match {
            case JsSuccess(GetTfcHistoryError(ServiceUnavailableDESHeader, ServiceUnavailableMessage), _) => ServiceUnavailableErr
            case _ => GetTfcHistoryUnexpectedError(SERVICE_UNAVAILABLE, response.body)
          })
        case status =>
          Left(GetTfcHistoryUnexpectedError(status, response.body))
      }
    }
  }

}


object GetTfcHistoryParser {

  type GetTfcHistoryResponse = Either[GetTfcHistoryFailure, TfcHistory]

  type TfcHistory = JsValue

  sealed trait GetTfcHistoryFailure

  case class GetTfcHistoryError(code: String, reason: String) extends GetTfcHistoryFailure

  object GetTfcHistoryError {
    implicit val format: OFormat[GetTfcHistoryError] = Json.format[GetTfcHistoryError]
  }

  case object InvalidNinoErr extends GetTfcHistoryFailure

  case object InvalidUcidErr extends GetTfcHistoryFailure

  case object InvalidOriginatorIdErr extends GetTfcHistoryFailure

  case object ServerErrorErr extends GetTfcHistoryFailure

  case object ServiceUnavailableErr extends GetTfcHistoryFailure

  case class BusinessValidationErr(message: String) extends GetTfcHistoryFailure

  case class NotFoundErr(message: String) extends GetTfcHistoryFailure

  val BusinessValidationErrMessageRegex = "^The back end has returned a business validation error:.*$"

  val NotFoundErrMessageRegex = "^The back end has returned a not found response:.*$"

  case class GetTfcHistoryUnexpectedError(status: Int, body: String) extends GetTfcHistoryFailure

}
