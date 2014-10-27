import com.github.athieriot.EmbedConnection
import controllers.Jobs
import models.Job
import org.specs2.mutable.Specification
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID
import services.JobService

import utils.MongoConnection._

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * Spec for job execution
 */
class JobDeamonSpec extends Specification with EmbedConnection {
  sequential

  val Port = 8080
  val Host = "localhost"

  val wireMockServer = new WireMockServer(wireMockConfig().port(Port))
  wireMockServer.start()
  WireMock.configureFor(Host, Port)

  "JobExecutionService#executeAll" should {
    "update Ads and send Email" in new EmbedSMTPContext {

      override val additionalConfiguration = testConfConnection

      running(fakeApp) {

        val content = """
          http://www.leboncoin.fr/ventes_immobilieres/0123456789.htm
          http://www.leboncoin.fr/ventes_immobilieres/9876543210.htm
        """

        val contentMatchA = "http://www.leboncoin.fr/ventes_immobilieres/0123456789.htm"
        val contentMatchB = "http://www.leboncoin.fr/ventes_immobilieres/9876543210.htm"

        stubFor(
          get(urlEqualTo("/"))
            .willReturn(
              aResponse()
                .withBody(content)
            )
        )

        val job = Job(Some(BSONObjectID.generate), "http://localhost:8080/", None)

        Await.result(Job.insert(job)(connection), Duration(10, "seconds"))
        Await.result(
          JobService.updateAds(
            job.id.get.stringify, Set("""http://www.leboncoin.fr/ventes_immobilieres/9876543210.htm""")
          )(connection), Duration.Inf
        )

        Await.result(Jobs.executeAllJobs, Duration(10, "seconds"))

        Await.result(lastReceivedMessage, Duration(10, "seconds")) must contain(contentMatchA)
        Await.result(lastReceivedMessage, Duration(10, "seconds")) must not contain(contentMatchB)

      }
    }
  }


}
