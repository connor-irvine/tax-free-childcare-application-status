# tax-free-childcare-application-status

This REST API allows consumers to retrieve information on the status of a current application for tax free childcare.

All end points are User Restricted (see [authorisation](https://developer.service.hmrc.gov.uk/api-documentation/docs/authorisation)). Versioning, data formats, etc. follow the API Platform standards (see [the reference guide](https://developer.service.hmrc.gov.uk/api-documentation/docs/reference-guide)).

For resources, error scenarios, and test data specific to this API, see documentation on the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api).


### Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs a [JRE](http://www.oracle.com/technetwork/java/javase/overview/index.html) to run.


### Running Locally

Install [Service Manager](https://github.com/hmrc/service-manager), then start dependencies:

    sm --start TFCAS_ALL -f

To start the app locally:

    sbt "run 9360"

### Running Tests

To run unit and integration tests:

    sbt test it:test

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
