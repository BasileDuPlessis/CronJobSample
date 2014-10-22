import com.github.athieriot.EmbedConnection
import models.Job
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.FakeApplication
import org.specs2.mutable.{ BeforeAfter, Specification }
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID

import utils.MongoConnection._

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
/**
 * Spec for job execution
 */
class JobExecutionServiceSpec extends Specification with EmbedConnection {
  sequential

  val Port = 8080
  val Host = "localhost"

  val wireMockServer = new WireMockServer(wireMockConfig().port(Port))
  wireMockServer.start()
  WireMock.configureFor(Host, Port)

  val conf = testConfConnection

  val fakeApp = FakeApplication(additionalConfiguration = conf)

  "JobExecutionService#executeAll" should {
    "update Ads and send Email" in running(fakeApp) {
      stubFor(
        get(urlEqualTo("/"))
          .willReturn(
            aResponse()
              .withBody("body")
          )
      )

      val job = Job(Some(BSONObjectID.generate), "url", None)

      Await.result(Job.insert(job)(connection), Duration.Inf)

      scala.io.Source.fromURL("http://localhost:8080/").getLines.mkString must beEqualTo("body")
    }
  }


}
