import models.Job
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.mockito.Mockito._
import org.mockito.Matchers
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.LastError

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
/**
 * Specifications for model job
 */
class JobModelSpec extends Specification with Mockito {

  "Job#insert" should {
    "call insert on collection" in {

      val mockDefaultDB = mock[DefaultDB]
      val mockCollection = mock[BSONCollection]
      val job = Job(None, "http://www.leboncoin.fr")

      when(
        mockDefaultDB[BSONCollection](anyString, any)(any)
      ) thenReturn mockCollection

      when(
        mockCollection.insert[Job](Matchers.eq(job), any)(any, any)
      ) thenReturn Future(new LastError(true, None, None, None, None, 0, false))

      Await.result(
        Job.insert(job)(mockDefaultDB), Duration.Inf
      )
      there was one(mockCollection).insert[Job](job)

    }
  }

}
