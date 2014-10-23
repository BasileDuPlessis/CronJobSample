import java.io.InputStream

import com.github.athieriot.EmbedConnection
import controllers.Jobs
import models.Job
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.subethamail.smtp.helper.{SimpleMessageListenerAdapter, SimpleMessageListener}
import org.subethamail.smtp.server.SMTPServer
import play.api.test.FakeApplication
import org.specs2.mutable.{ BeforeAfter, Specification }
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID

import utils.MongoConnection._

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._

import scala.concurrent.{Promise, Await}
import scala.concurrent.duration.Duration
import scala.io.Source

@RunWith(classOf[JUnitRunner])
/**
 * Spec for job execution
 */
class JobExecutionServiceSpec extends Specification with EmbedConnection {
  sequential

  val promiseMessage = Promise[String]
  val lastReceivedMessage = promiseMessage.future

  val messageListener = new SimpleMessageListener {
    def accept(from: String, recipient: String): Boolean = true
    def deliver(from: String, recipient: String, data: InputStream): Unit = {
      if (!promiseMessage.isCompleted) promiseMessage.success(Source.fromInputStream(data).mkString)
    }
  }

  val smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageListener))
  smtpServer.start()

  val Port = 8080
  val Host = "localhost"

  val wireMockServer = new WireMockServer(wireMockConfig().port(Port))
  wireMockServer.start()
  WireMock.configureFor(Host, Port)

  val conf =  Map("smtp.host" -> smtpServer.getHostName) ++ testConfConnection

  val fakeApp = FakeApplication(additionalConfiguration = conf)

  "JobExecutionService#executeAll" should {
    "update Ads and send Email" in context {
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

        val job = Job(None, "http://localhost:8080/", None)

        Await.result(Job.insert(job)(connection), Duration.Inf)

        Await.result(Jobs.executeAllJobs, Duration.Inf)

        Await.result(lastReceivedMessage, Duration.Inf) must contain(contentMatchA)
        Await.result(lastReceivedMessage, Duration.Inf) must contain(contentMatchB)

      }
    }
  }

  object context extends BeforeAfter {

    def before = {
    }

    def after = {
    }

  }

}
