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
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.api.collections.GenericQueryBuilder
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
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

  "Recipe#readOne(BSONObjectID)" should {
    "call find with id parameter on collection" in {
      val mockDefaultDB = mock[DefaultDB]
      val mockCollection = mock[BSONCollection]
      val id = BSONObjectID.generate
      val job = Job(Some(id), "http://www.leboncoin.fr")

      when(
        mockDefaultDB[BSONCollection](anyString, any)(any)
      ) thenReturn mockCollection

      val genericQueryBuilder = mock[GenericQueryBuilder[BSONDocument, BSONDocumentReader, BSONDocumentWriter]]

      when(
        mockCollection.find(Matchers.eq(BSONDocument("_id" -> id)))(any)
      ) thenReturn genericQueryBuilder

      when(
        genericQueryBuilder.one[Job](any, any)
      ) thenReturn Future(Some(job))

      Await.result(
        Job.read(id)(mockDefaultDB), Duration.Inf
      )

      there was one(mockCollection).find(BSONDocument("_id" -> id))
    }
  }

  "Recipe#readAll" should {
    "return all jobs in a list" in {
      val mockDefaultDB = mock[DefaultDB]
      val mockCollection = mock[BSONCollection]
      val genericQueryBuilder = mock[GenericQueryBuilder[BSONDocument, BSONDocumentReader, BSONDocumentWriter]]
      val mockCursor = mock[Cursor[Job]]

      when(
        mockDefaultDB[BSONCollection](anyString, any)(any)
      ) thenReturn mockCollection

      when(
        mockCollection.find(Matchers.eq(BSONDocument()))(any)
      ) thenReturn genericQueryBuilder

      when(
        genericQueryBuilder.cursor[Job](any, any)
      ) thenReturn mockCursor

      when(
        mockCursor.collect[List](any, any)(any, any)
      ) thenReturn Future(List[Job]())

      Await.result(
        Job.readAll(mockDefaultDB), Duration.Inf
      )

      there was one(mockCollection).find(BSONDocument())

    }
  }

}
