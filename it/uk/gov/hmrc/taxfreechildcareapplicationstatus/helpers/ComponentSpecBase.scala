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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.{Application, Environment, Mode}

trait ComponentSpecBase extends TestSuite with GuiceOneServerPerSuite with WiremockHelper
  with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures {

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(15, Millis))

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWiremock()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  def config: Map[String, String] = Map(
    "auditing.enabled" -> "false",
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.des.host" -> mockHost,
    "microservice.services.des.port" -> mockPort
  )

  def buildClientApp(path: String, headers: Map[String, String]): WSRequest =
    ws.url(s"http://localhost:$port/tax-free-childcare-applicant$path")
      .withHttpHeaders(
        headers.map {
          case (key, value) => (key, value)
        }.toSeq: _*
      ).withFollowRedirects(false)

  def get[T](uri: String, headers: Map[String, String] = Map.empty): WSResponse = buildClientApp(uri, headers).get.futureValue

  def buildClientRoot(path: String, headers: Map[String, String]): WSRequest =
    ws.url(s"http://localhost:$port$path")
      .withHttpHeaders(
        headers.map {
          case (key, value) => (key, value)
        }.toSeq: _*
      ).withFollowRedirects(false)

  def rootGet[T](uri: String, headers: Map[String, String] = Map.empty): WSResponse = buildClientRoot(uri, headers).get.futureValue

}
