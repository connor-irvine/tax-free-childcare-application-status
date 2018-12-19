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

import java.nio.charset.Charset

import akka.stream.Materializer
import akka.util.ByteString
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier}
import uk.gov.hmrc.taxfreechildcareapplicationstatus.connectors.mocks.MockTfcasAuthConnector
import uk.gov.hmrc.taxfreechildcareapplicationstatus.controllers.GetTfcHistoryController._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.TestConstants._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.httpparsers.GetTfcHistoryParser._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.models.ApiPlatformErrorResponse._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.services.mocks.MockGetTfcHistoryService

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class GetTfcHistoryControllerSpec extends WordSpecLike with Matchers with GuiceOneAppPerSuite
  with MockTfcasAuthConnector with MockGetTfcHistoryService {

  lazy val cc: ControllerComponents = app.injector.instanceOf[ControllerComponents]
  lazy implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  lazy implicit val materializer: Materializer = app.injector.instanceOf[Materializer]

  object TestController extends GetTfcHistoryController(cc, mockAuthConnector, mockGetTfcHistoryService, ec)

  private val defaultTimeOut: Duration = 5 seconds

  private def await[T](f: Future[T]): T = Await.result(f, defaultTimeOut)

  private def status[T](r: Result): Int = r.header.status

  private def status[T](f: Future[Result]): Int = await(f.map(status))

  private def jsonBodyOf[T](r: Result): JsValue = {
    val bodyBytes: ByteString = await(r.body.consumeData)
    val bodyString = bodyBytes.decodeString(Charset.defaultCharset().name)
    Json.parse(bodyString)
  }

  private def jsonBodyOf[T](f: Future[Result]): JsValue = await(f.map(jsonBodyOf))

  implicit val hc = HeaderCarrier()
  val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(originatorId -> testOriginatorId, correlationId -> testCorrelationId)

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockAuth()
  }

  "GetTfcHistoryController.getTfcHistory with valid a user request" when {
    "GetTfcHistoryService returns a Right" should {
      "return OK with the returned json" in {
        val testJson = Json.obj()
        stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Right(testJson)))
        val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe OK
        jsonBodyOf(result) shouldBe testJson
      }
    }
    "GetTfcHistoryService returns a Left invalid nino" should {
      "return BAD_REQUEST with the invalid nino json" in {
        stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Left(InvalidNinoErr)))
        val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe BAD_REQUEST
        jsonBodyOf(result) shouldBe Json.toJson(InvalidNinoResponse)
      }
    }
    "GetTfcHistoryService returns a Left invalid UCID" should {
      "return BAD_REQUEST with the invalid ucid json" in {
        stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Left(InvalidUcidErr)))
        val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe BAD_REQUEST
        jsonBodyOf(result) shouldBe Json.toJson(InvalidUcidResponse)
      }
    }
    "GetTfcHistoryService returns a Left invalid originator Id" should {
      "return BAD_REQUEST with the invalid originator id json" in {
        stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Left(InvalidOriginatorIdErr)))
        val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe BAD_REQUEST
        jsonBodyOf(result) shouldBe Json.toJson(InvalidOriginatorIdResponse)
      }
    }
    "GetTfcHistoryService returns a Left business validation" should {
      "return BAD_REQUEST with the business validation json" in {
        val errMessage = BusinessValidationErrMessageRegex.replace(".*", "failure reason").filterNot(x => x == '^' | x == '$')
        val businessValidationResponse = BusinessValidationErr(errMessage)

        stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Left(businessValidationResponse)))
        val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe BAD_REQUEST
        jsonBodyOf(result) shouldBe Json.toJson(BusinessValidationResponse(errMessage))
      }
    }
    "GetTfcHistoryService returns a Left not found" should {
      "return NOT_FOUND with the not found json" in {
        val errMessage = NotFoundErrMessageRegex.replace(".*", "failure reason").filterNot(x => x == '^' | x == '$')
        val notFoundResponse = NotFoundErr(errMessage)

        stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Left(notFoundResponse)))
        val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe NOT_FOUND
        jsonBodyOf(result) shouldBe Json.toJson(NotFoundResponse(errMessage))
      }
    }
    "GetTfcHistoryService returns a Left server error" should {
      "return INTERNAL_SERVER_ERROR with the internal server error json" in {
        stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Left(ServerErrorErr)))
        val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        jsonBodyOf(result) shouldBe Json.toJson(InternalServerErrorResponse)
      }
    }
    "GetTfcHistoryService returns a Left service unavailable" should {
      "return SERVICE_UNAVAILABLE with the service unavailable json" in {
        stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Left(ServiceUnavailableErr)))
        val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe SERVICE_UNAVAILABLE
        jsonBodyOf(result) shouldBe Json.toJson(ServiceUnavailableResponse)
      }
    }
    "GetTfcHistoryService returns a Left GetTfcHistoryUnexpectedError" when {
      "the status is OK" should {
        "return 500 with the UnexpectedServerErrorResponse" in {
          stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Left(GetTfcHistoryUnexpectedError(OK, ""))))
          val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

          status(result) shouldBe INTERNAL_SERVER_ERROR
          jsonBodyOf(result) shouldBe Json.toJson(UnexpectedServerErrorResponse)
        }
      }
      "the status is not OK" should {
        "return 500 with the UnexpectedServerErrorResponse" in {
          stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.successful(Left(GetTfcHistoryUnexpectedError(INTERNAL_SERVER_ERROR, ""))))
          val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

          status(result) shouldBe INTERNAL_SERVER_ERROR
          jsonBodyOf(result) shouldBe Json.toJson(UnexpectedServerErrorResponse)
        }
      }
    }
    "GetTfcHistoryService thrown and exception" when {
      "return 500 with the exception message" in {
        stubGetClaimsHistory(testNino, testUniqueClaimId)(Future.failed(new BadGatewayException("")))
        val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        jsonBodyOf(result) shouldBe Json.toJson(UnexpectedServerErrorResponse)
      }
    }
  }

  "GetTfcHistoryController.getTfcHistory will reject the user's request" when {
    "nino is invalid" should {
      "return BAD_REQUEST with the invalid nino json" in {
        val invalidNino = "AAA"

        val result = TestController.getTfcHistory(invalidNino, testUniqueClaimId)(testRequest)

        status(result) shouldBe BAD_REQUEST
        jsonBodyOf(result) shouldBe Json.toJson(InvalidNinoResponse)
      }
    }
    "UCID is invalid" should {
      "return BAD_REQUEST with the invalid UCID json" in {
        val invalidUCID = "1" * 10

        val result = TestController.getTfcHistory(testNino, invalidUCID)(testRequest)

        status(result) shouldBe BAD_REQUEST
        jsonBodyOf(result) shouldBe Json.toJson(InvalidUcidResponse)
      }
    }
    "Correlation id is invalid" when {
      "it is not provided" should {
        "return BAD_REQUEST" in {
          val testRequest = FakeRequest().withHeaders(originatorId -> testOriginatorId)
          val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

          status(result) shouldBe BAD_REQUEST
          //TODO add test for the json body once it's defined
        }
      }
      "it is does not match the regex" should {
        "return BAD_REQUEST" in {
          val invalidCorrelationId = "1" * 10
          val testRequest = FakeRequest().withHeaders(originatorId -> testOriginatorId, correlationId -> invalidCorrelationId)
          val result = TestController.getTfcHistory(testNino, testUniqueClaimId)(testRequest)

          status(result) shouldBe BAD_REQUEST
          //TODO add test for the json body once it's defined
        }
      }
    }
  }

}
