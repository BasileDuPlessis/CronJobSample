import models.Job
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import com.github.athieriot._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
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
    "add a pattern field to a job without one" in {

      val ids = for (i <- 1 to 2) yield BSONObjectID.generate

      ids.foreach {
        id => Await.result(
          connection[BSONCollection]("jobs").insert(
            BSONDocument(
              "_id" -> Some(id),
              "url" -> "url",
              "ads" -> None
            )
          ),
          Duration(20, "seconds")
        )
      }
//test git
      val idNewJob = BSONObjectID.generate
      val job = Job(Some(idNewJob), "url", Some(List()), "newPattern")
      Await.result(Job.insert(job)(connection), Duration(20, "seconds"))


      Await.result(
        Job.addDefaultPatternToUndefinedPatternFields("pattern")(connection),
        Duration(20, "seconds")
      )

      ids.map {
        id => Await.result(
          Job.read(id)(connection), Duration(20, "seconds")
        ).get.pattern
      } must beEqualTo(Seq("pattern", "pattern"))
//test local
      Await.result(
        Job.read(idNewJob)(connection), Duration(20, "seconds")
      ).get must beEqualTo(job)

    }
  }


}
