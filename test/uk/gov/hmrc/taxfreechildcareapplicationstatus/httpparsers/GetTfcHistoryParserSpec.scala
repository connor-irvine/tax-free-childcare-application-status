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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers

import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.taxfreechildcareapplicationstatus.TfcasConstants._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers.GetTfcHistoryParser._

import scala.io.Source

class GetTfcHistoryParserSpec extends WordSpec with Matchers with GuiceOneAppPerSuite {

  val testHttpVerb = "GET"
  val testUri = "/"

  lazy val parser: GetTfcHistoryParser = app.injector.instanceOf[GetTfcHistoryParser]

  import parser._

  lazy val testDesResponse: JsValue = Json.parse(Source.fromFile("test/resources/des/200-success.json").getLines.mkString)
  lazy val testDesInvalidNinoResponse: JsValue = Json.parse(Source.fromFile("test/resources/des/400-invalid-nino.json").getLines.mkString)
  lazy val testDesInvalidUcidResponse: JsValue = Json.parse(Source.fromFile("test/resources/des/400-invalid-ucid.json").getLines.mkString)
  lazy val testDesInvalidOriginatorIdResponse: JsValue = Json.parse(Source.fromFile("test/resources/des/400-invalid-originator-id.json").getLines.mkString)
  lazy val testDesInternalServerResponse: JsValue = Json.parse(Source.fromFile("test/resources/des/500-internal-server-error.json").getLines.mkString)
  lazy val testDesServiceUnavailableResponse: JsValue = Json.parse(Source.fromFile("test/resources/des/503-service-unavailable.json").getLines.mkString)

  "GetTfcHistoryRequestHttpReads" when {
    "read" should {
      "parse an OK response with a valid payload as a JsonValue" in {
        val httpResponse = HttpResponse(OK, Some(testDesResponse))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Right(testDesResponse)
      }

      "parse an OK response with an invalid payload as a GetTfcHistoryUnexpectedError" in {
        val testResponseBody = "{}"
        val httpResponse = HttpResponse(OK, Some(Json.toJson(testResponseBody)))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(GetTfcHistoryUnexpectedError(OK, Json.prettyPrint(Json.toJson(testResponseBody))))
      }

      "parse a BAD_REQUEST with invalid nino response as InvalidNinoErr" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(testDesInvalidNinoResponse))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(InvalidNinoErr)
      }

      "parse a BAD_REQUEST with invalid UCID response as InvalidUcidErr" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(testDesInvalidUcidResponse))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(InvalidUcidErr)
      }

      "parse a BAD_REQUEST with invalid originator id response as InvalidOriginatorIdErr" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(testDesInvalidOriginatorIdResponse))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(InvalidOriginatorIdErr)
      }

      "parse a BAD_REQUEST with BusinessValidationErrCode and a valid err message response as a BusinessValidationErr" in {
        val testErrMsg = BusinessValidationErrMessageRegex.replace(".*", "*failure reason*").filterNot(c => c == '^' || c == '$')
        val testResponseBody = GetTfcHistoryError(BusinessValidationHeader, testErrMsg)
        val httpResponse = HttpResponse(BAD_REQUEST, Some(Json.toJson(testResponseBody)))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(BusinessValidationErr(testErrMsg))
      }

      "parse a BAD_REQUEST with BusinessValidationErrCode and a invalid err message response as a GetTfcHistoryUnexpectedError" in {
        val testErrMsg = "invalid message"
        val testResponseBody = GetTfcHistoryError(BusinessValidationHeader, testErrMsg)
        val httpResponse = HttpResponse(BAD_REQUEST, Some(Json.toJson(testResponseBody)))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(GetTfcHistoryUnexpectedError(BAD_REQUEST, Json.prettyPrint(Json.toJson(testResponseBody))))
      }

      "parse a BAD_REQUEST with an invalid code as GetTfcHistoryUnexpectedError" in {
        val testErrCode = "invalid code"
        val testErrMsg = "invalid message"
        val testResponseBody = GetTfcHistoryError(testErrCode, testErrMsg)
        val httpResponse = HttpResponse(BAD_REQUEST, Some(Json.toJson(testResponseBody)))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(GetTfcHistoryUnexpectedError(BAD_REQUEST, Json.prettyPrint(Json.toJson(testResponseBody))))
      }

      "parse a NOT_FOUND with NotFoundErrCode and valid err message response as a NotFoundErr" in {
        val testErrMsg = NotFoundErrMessageRegex.replace(".*", "*failure reason*").filterNot(c => c == '^' || c == '$')
        val testResponseBody = GetTfcHistoryError(NotFoundHeader, testErrMsg)
        val httpResponse = HttpResponse(NOT_FOUND, Some(Json.toJson(testResponseBody)))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(NotFoundErr(testErrMsg))
      }

      "parse a NOT_FOUND with an invalid response as GetTfcHistoryUnexpectedError" in {
        val testErrMsg = "invalid message"
        val testResponseBody = GetTfcHistoryError(NotFoundHeader, testErrMsg)
        val httpResponse = HttpResponse(NOT_FOUND, Some(Json.toJson(testResponseBody)))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(GetTfcHistoryUnexpectedError(NOT_FOUND, Json.prettyPrint(Json.toJson(testResponseBody))))
      }

      "parse a INTERNAL_SERVER_ERROR with server error response as ServerErrorErr" in {
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Some(testDesInternalServerResponse))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(ServerErrorErr)
      }

      "parse a INTERNAL_SERVER_ERROR with an invalid response as GetTfcHistoryUnexpectedError" in {
        val testErrCode = "invalid code"
        val testErrMsg = "invalid message"
        val testResponseBody = GetTfcHistoryError(testErrCode, testErrMsg)
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Some(Json.toJson(testResponseBody)))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(GetTfcHistoryUnexpectedError(INTERNAL_SERVER_ERROR, Json.prettyPrint(Json.toJson(testResponseBody))))
      }

      "parse a SERVICE_UNAVAILABLE with server unavailable response as ServiceUnavailableErr" in {
        val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, Some(testDesServiceUnavailableResponse))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(ServiceUnavailableErr)
      }

      "parse a SERVICE_UNAVAILABLE with an invalid response as GetTfcHistoryUnexpectedError" in {
        val testErrCode = "invalid code"
        val testErrMsg = "invalid message"
        val testResponseBody = GetTfcHistoryError(testErrCode, testErrMsg)
        val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, Some(Json.toJson(testResponseBody)))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(GetTfcHistoryUnexpectedError(SERVICE_UNAVAILABLE, Json.prettyPrint(Json.toJson(testResponseBody))))
      }

      "parse a invalid status as GetTfcHistoryUnexpectedError" in {
        val testErrCode = "invalid code"
        val testErrMsg = "invalid message"
        val testResponseBody = GetTfcHistoryError(testErrCode, testErrMsg)
        val httpResponse = HttpResponse(NOT_IMPLEMENTED, Some(Json.toJson(testResponseBody)))

        val res = GetTfcHistoryRequestHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(GetTfcHistoryUnexpectedError(NOT_IMPLEMENTED, Json.prettyPrint(Json.toJson(testResponseBody))))
      }
    }
  }

}
