package uk.gov.hmrc.taxfreechildcareapplicationstatus.controllers

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.taxfreechildcareapplicationstatus.helpers.{ComponentSpecBase, CustomMatchers}
import play.api.http.Status._
import uk.gov.hmrc.taxfreechildcareapplicationstatus.config.AppConfig
import uk.gov.hmrc.taxfreechildcareapplicationstatus.views.txt

class DocumentationControllerISpec extends WordSpec with Matchers with ComponentSpecBase with CustomMatchers {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "/api/definition" should {
    "return OK with the definition" in {
      val result = rootGet("/api/definition")

      result should have(
        httpStatus(OK),
        bodyOf(txt.definition(appConfig.apiAccess, appConfig.apiContext).toString())
      )
    }
  }

}
