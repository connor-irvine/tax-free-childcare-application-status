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

import org.scalatest.{Matchers, WordSpec}
import play.api.http.Status._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.{ComponentSpecBase, CustomMatchers}

class MicroserviceHelloWorldISpec extends WordSpec with Matchers with ComponentSpecBase with CustomMatchers {

  "GET /hello-world" should {
    "return a status of OK" in {

      val result = get("/hello-world")

      result should have(
        httpStatus(OK)
      )
    }
  }

}