import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import com.github.athieriot._

import utils.MongoConnection.testConfConnection

/**
 * Functional spec for Job controller
 */
class JobFunctionalSpec extends Specification with EmbedConnection {
  sequential

  val conf = Map() ++ testConfConnection

  val fakeApp = FakeApplication(additionalConfiguration = conf)

  step(play.api.Play.start(fakeApp))

  "Job controller" should {

    "create a new Job on POST /job/create and redirect to /job/:id" in {
      val jobCreate = route(FakeRequest(POST, "/job/create").withFormUrlEncodedBody(
        ("url", "testUrl"),
        ("pattern", "testPattern")
      )).get

      status(jobCreate) must equalTo(SEE_OTHER)

      redirectLocation(jobCreate) must beSome[String].which(_.matches("/job/[a-z0-9]{24}$"))

    }

    "show a creating form on GET /job/" in {
      val jobGetCreate = route(FakeRequest(GET, "/job/")).get

      status(jobGetCreate) must equalTo(OK)

      contentAsString(jobGetCreate) must contain("Create job")

    }

    "show job details on GET /job/:id" in {
      val jobCreate = route(FakeRequest(POST, "/job/create").withFormUrlEncodedBody(
        ("url", "testUrl"),
        ("pattern", "testPattern")
      )).get

      val id = "/job/(.+)$".r.findFirstMatchIn(redirectLocation(jobCreate).get).get.group(1)

      val jobDetails = route(FakeRequest(GET, s"/job/$id")).get

      status(jobDetails) must equalTo(OK)

      contentAsString(jobDetails) must contain("testUrl")

    }

  }

}
