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

  "Job controller" should {

    "create a new Job and redirect to /job/:id" in new WithApplication(
      FakeApplication(additionalConfiguration = conf)
    ) {
      val jobCreate = route(FakeRequest(POST, "/job/create").withFormUrlEncodedBody(
        ("url", "http://www.leboncoin.fr")
      )).get

      status(jobCreate) must equalTo(SEE_OTHER)

    }

  }

}
