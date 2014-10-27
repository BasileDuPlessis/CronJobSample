package models

import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import scala.concurrent.Future

import _root_.utils.CustomMappings._

import play.api.libs.concurrent.Execution.Implicits.defaultContext


/**
 * Model Job
 */

case class Job(
  id: Option[BSONObjectID],
  url: String,
  ads: Option[List[String]],
  pattern: String
)


object Job {

  private val collectionName = "jobs"

  implicit object RecipeBSONReader extends BSONDocumentReader[Job] {
    def read(doc: BSONDocument): Job =
      Job(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("url").get,
        doc.getAs[List[String]]("ads"),
        doc.getAs[String]("pattern").get
      )
  }

  implicit object RecipeBSONWriter extends BSONDocumentWriter[Job] {

    def write(job: Job): BSONDocument =
      BSONDocument(
        "_id" -> job.id.getOrElse(BSONObjectID.generate),
        "url" -> job.url,
        "ads" -> job.ads.getOrElse(List[String]()),
        "pattern" -> job.pattern
      )
  }

  def insert(job: Job): DefaultDB => Future[LastError] = {
    db:DefaultDB =>  db[BSONCollection](collectionName).insert[Job](job)
  }

  def update(id: BSONObjectID, modifier: BSONDocument): DefaultDB => Future[LastError] = {
    db:DefaultDB =>  db[BSONCollection](collectionName).update(BSONDocument("_id" -> id), modifier)
  }



  def addDefaultPatternToUndefinedPatternFields(pattern: String): DefaultDB => Future[LastError] = {
    db:DefaultDB =>  db[BSONCollection](collectionName).update(
      BSONDocument("pattern" -> BSONDocument("$exists" -> false)),
      BSONDocument("pattern" -> pattern)
    )
  }

  /**
   * Read a job from id
   */
  def read(id: BSONObjectID): DefaultDB => Future[Option[Job]]= {
    db:DefaultDB => db[BSONCollection](collectionName).find(BSONDocument("_id" -> id)).one[Job]
  }

  /**
   * Read all jobs
   */
  def readAll: DefaultDB => Future[List[Job]]= {
    db:DefaultDB => db[BSONCollection](collectionName).find(BSONDocument()).cursor[Job].collect[List]()
  }

  val jobForm = Form(
    mapping(
      "id" -> optional(of[BSONObjectID]),
      "url" -> text,
      "results" -> optional(list(text)),
      "pattern" -> text
    )(Job.apply)(Job.unapply)
  )

}