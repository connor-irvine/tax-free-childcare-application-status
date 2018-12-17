import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"           % "0.32.0",
    "uk.gov.hmrc"             %% "play-hmrc-api"               % "3.3.0-play-26",
    "com.eclipsesource"       %% "play-json-schema-validator"  % "0.9.4"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"                   % "3.0.4"                 % "test",
    "com.typesafe.play"       %% "play-test"                   % current                 % "test",
    "org.pegdown"             %  "pegdown"                     % "1.6.0"                 % "test, it",
    "com.github.tomakehurst"  %  "wiremock"                    % "2.19.0"                % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"          % "3.1.2"                 % "test, it",
    "uk.gov.hmrc"             %% "domain"                      % "5.2.0"                 % "test, it",
    "org.mockito"             %  "mockito-core"                % "2.23.4"                % "test, it"
  )

  // Fixes a transitive dependency clash between wiremock and scalatestplus-play
  val overrides: Set[ModuleID] = {
    val jettyFromWiremockVersion = "9.2.24.v20180105"
    Set(
      "org.eclipse.jetty"           % "jetty-client"       % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-continuation" % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-http"         % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-io"           % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-security"     % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-server"       % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-servlet"      % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-servlets"     % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-util"         % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-webapp"       % jettyFromWiremockVersion,
      "org.eclipse.jetty"           % "jetty-xml"          % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-api"      % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-client"   % jettyFromWiremockVersion,
      "org.eclipse.jetty.websocket" % "websocket-common"   % jettyFromWiremockVersion
    )
  }
}
