import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import com.github.athieriot._

/**
 * Functional spec for Job controller
 */
class JobFunctionalSpec extends Specification with EmbedConnection {
  sequential

  val conf = Map(
    "mongodb.servers" -> List("localhost:12345")
  )

  val fakeApp = FakeApplication(additionalConfiguration = conf)

  "Job controller" should {

    "create a new Job on POST /job/create and redirect to /job/:id" in new WithApplication(fakeApp) {
      val jobCreate = route(FakeRequest(POST, "/job/create").withFormUrlEncodedBody(
        ("url", "http://www.leboncoin.fr")
      )).get

      status(jobCreate) must equalTo(SEE_OTHER)

      redirectLocation(jobCreate) must beSome[String].which(_.matches("/job/[a-z0-9]{24}$"))
    }

    "show a creating form on GET /job/" in new WithApplication(fakeApp) {
      val jobCreate = route(FakeRequest(GET, "/job/")).get

      status(jobCreate) must equalTo(OK)

      contentAsString(jobCreate) must contain("Create job")
    }

  }

}
