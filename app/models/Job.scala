package models

import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.core.commands.LastError



import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext


/**
 * Model Job
 */

case class Job(
  id: Option[BSONObjectID],
  url: String
)


object Job {

  private val collectionName = "jobs"

  implicit object RecipeBSONReader extends BSONDocumentReader[Job] {
    def read(doc: BSONDocument): Job =
      Job(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("url").get
      )
  }

  implicit object RecipeBSONWriter extends BSONDocumentWriter[Job] {
    def write(job: Job): BSONDocument =
      BSONDocument(
        "_id" -> job.id.getOrElse(BSONObjectID.generate),
        "url" -> job.url
      )
  }

  def insert(job: Job): DefaultDB => Future[LastError] = {
    db:DefaultDB =>  db[BSONCollection](collectionName).insert[Job](job)
  }

}