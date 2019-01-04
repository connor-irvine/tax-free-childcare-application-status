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

package uk.gov.hmrc.taxfreechildcareapplicationstatus

object TfcasConstants {

  val InvalidNinoHeader = "INVALID_NINO"
  val InvalidNinoMessage = "Submission has not passed validation. Invalid nino."

  val InvalidUcidHeader = "INVALID_UCID"
  val InvalidUcidMessage = "Submission has not passed validation. Invalid Unique Claim Id."

  val InvalidOriginatorIdHeader = "INVALID_ORIGINATOR_ID"
  val InvalidOriginatorIdMessage = "Submission has not passed validation. Invalid header Originator-Id."

  val BusinessValidationHeader = "BUSINESS_VALIDATION"
  val NotFoundHeader = "NOT_FOUND"

  val InternalServerErrorAPIHeader = "INTERNAL_SERVER_ERROR"
  val InternalServerErrorDESHeader = "SERVER_ERROR"
  val InternalServerErrorMessage = "DES is currently experiencing problems that require live service intervention."

  val ServiceUnavailableAPIHeader = "SERVER_ERROR"
  val ServiceUnavailableDESHeader = "SERVICE_UNAVAILABLE"
  val ServiceUnavailableMessage = "Dependent systems are currently not responding"

}
