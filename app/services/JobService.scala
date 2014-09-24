package services

import libraries.Di._
import libraries.Di.Reader

import models.Job
import reactivemongo.api.DefaultDB

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import scala.concurrent.Future


/**
 * Provide application logic for recipes
 */
object JobService {


  /**
   * Try to parse job id as a BSONObjectID and read job
   */
  def readFromId(id: String): Reader[DefaultDB, Future[Option[Job]]] =
    for {
      tryId <- pure(BSONObjectID.parse(id))
      result <- tryId map {oid => Job.read(oid)}
    } yield result


  def updateAds(id: String, ads: List[String]): Reader[DefaultDB, Future[LastError]] = {
    for {
      tryId <- pure(BSONObjectID.parse(id))
      result <- tryId map {
        oid => Job.update(
          oid,
          BSONDocument("$addToSet" -> BSONDocument("ads" -> BSONDocument("$each" -> ads)))
        )
      }
    } yield result

  }


}