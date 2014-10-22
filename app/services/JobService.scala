package services

import com.typesafe.plugin.MailerAPI
import libraries.Di._
import libraries.Di.Reader

import models.Job
import reactivemongo.api.DefaultDB

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import scala.concurrent.Future
import scala.io.Source


/**
 * Provide application logic for recipes
 */
object JobService {


  /**
   * Try to parse job id as a BSONObjectID and read job
   */
  def readFromId(id: String): Reader[DefaultDB, Future[Option[Job]]] =
    for {
      futureId <- Future(BSONObjectID.parse(id).get)
    } yield Job.read(futureId)


  def updateAds(id: String, ads: Set[String]): Reader[DefaultDB, Future[LastError]] = {
    for {
      futureId <- Future(BSONObjectID.parse(id).get)
    } yield Job.update(
      futureId,
      BSONDocument("$addToSet" -> BSONDocument("ads" -> BSONDocument("$each" -> ads)))
    )
  }




}