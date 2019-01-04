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

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import uk.gov.hmrc.taxfreechildcareapplicationstatus.connectors.mocks.MockServiceLocatorConnector

class ApplicationRegistrationSpec extends WordSpecLike with Matchers with GuiceOneAppPerSuite
  with MockServiceLocatorConnector {

  def testApplicationRegistration(enabledRegistration: Boolean): ApplicationRegistration =
    new ApplicationRegistration(
      new ServicesConfig(
        app.injector.instanceOf[Configuration],
        app.injector.instanceOf[RunMode]
      ) {
        override def getConfBool(confKey: String, defBool: => Boolean): Boolean =
          confKey match {
            case "service-locator.enabled" => enabledRegistration
            case _ => false
          }
      },
      mockServiceLocatorConnector
    )

  "ApplicationRegistration" should {
    "try to register on service locator if configured" in {
      val applicationRegistration = testApplicationRegistration(enabledRegistration = true)
      applicationRegistration.registrationEnabled should be(true)
      verify(mockServiceLocatorConnector).register(ArgumentMatchers.any[HeaderCarrier])
    }

    "not try to register on service locator if not configured" in {
      val applicationRegistration = testApplicationRegistration(enabledRegistration = false)
      applicationRegistration.registrationEnabled should be(false)
      verify(mockServiceLocatorConnector, never()).register(ArgumentMatchers.any[HeaderCarrier])
    }
  }

}
