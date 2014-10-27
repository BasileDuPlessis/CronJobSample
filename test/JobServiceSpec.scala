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
class JobServiceSpec extends Specification with EmbedConnection {
  sequential

  val connection = testConnection

  "JobService#readFromId" should {
    "read a job from a string ID" in {

      val job = Job(Some(BSONObjectID.generate), "url", None, "")

      Await.result(Job.insert(job)(connection), Duration.Inf)

      Await.result(
        JobService.readFromId(job.id.get.stringify)(connection), Duration.Inf
      ).get.id must beEqualTo(job.id)

    }

    "Recover an exception with a wrong BSONID" in {
      Await.result(
        JobService.readFromId("BSON ID")(connection).recover{case e => e.getMessage}, Duration.Inf
      ) must beEqualTo("wrong ObjectId: 'BSON ID'")
    }

    "Map a None value with a non existant BSONID" in {
      val id = BSONObjectID.generate
      Await.result(
        JobService.readFromId(id.stringify)(connection).map{job => job}, Duration.Inf
      ) must beNone
    }
  }

  "JobService#upddateAds" should {
    "Add non existant ads in ads field" in {

      val job = Job(Some(BSONObjectID.generate), "url", None, "")

      Await.result(Job.insert(job)(connection), Duration.Inf)

      Await.result(
        JobService.updateAds(job.id.get.stringify, Set("A", "B", "A"))(connection), Duration.Inf
      )

      Await.result(
        JobService.readFromId(job.id.get.stringify)(connection), Duration.Inf
      ).get.ads.get must beEqualTo(List("A", "B"))

    }
  }

}