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

package uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers

import java.util.UUID

import uk.gov.hmrc.domain.Generator

import scala.util.Random

object IntegrationTestConstants {

  val testNino: String = new Generator().nextNino.nino
  val testUniqueClaimId: String = f"${Random.nextInt(Math.pow(10, 11).toInt)}%011d"
  val testOriginatorId: String = UUID.randomUUID().toString
  val testCorrelationId: String = UUID.randomUUID().toString

}
