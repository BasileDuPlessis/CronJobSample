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

import play.api.Logger

@RunWith(classOf[JUnitRunner])
/**
 * Functional spec for Job controller
 */
class MigrationSpec extends Specification with EmbedConnection {
  sequential

  val connection = testConnection

  "Jobs#migrationAddPattern" should {
    "do not change add a pattern field with a default value" in {

      val job = Job(Some(BSONObjectID.generate), "url", None, "")

      //Await.result(connection.collection("jobs").db.command())

      Await.result(
        JobService.readFromId(job.id.get.stringify)(connection), Duration.Inf
      ).get.id must beEqualTo(job.id)

    }
  }


}