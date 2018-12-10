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
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort
  )

  def buildClient(path: String): WSRequest = ws.url(s"http://localhost:$port/tax-free-childcare-application-status$path").withFollowRedirects(false)

  def get[T](uri: String): WSResponse = buildClient(uri).get.futureValue

}
