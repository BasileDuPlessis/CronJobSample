import models.Job
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import com.github.athieriot._
import reactivemongo.bson.BSONObjectID
import services.JobService
import utils.MongoConnection._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
/**
 * Functional spec for Job controller
 */
class JobServiceSpec extends Specification with EmbedConnection {
  sequential

  val connection = testConnection

  val job = Job(Some(BSONObjectID.generate), "http://www.leboncoin.fr", None)

  "JobService.readFromId" should {
    "read a job from a string ID" in {

      Await.result(Job.insert(job)(connection), Duration.Inf)

      Await.result(
        JobService.readFromId(job.id.get.stringify)(connection), Duration.Inf
      ).get.id must beEqualTo(job.id)

    }
  }


}