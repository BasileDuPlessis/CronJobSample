package services

import models.Job
import org.apache.commons.mail.EmailException
import play.api.Logger
import reactivemongo.api.DefaultDB
import reactivemongo.core.commands.LastError
import utils.MongoConnection._
import scala.concurrent.Future
import scala.io.Source

import com.typesafe.plugin._
import play.api.Play.current

import scala.util.{Failure, Try}

import libraries.Di._
import libraries.Di.Reader

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Parse page and get new items
 */
object BoncoinService {

  //Get Ads from an url
  def parseAds(s: String): Set[String] = """(http://www.leboncoin.fr/ventes_immobilieres/[0-9]+\.htm)""".r.findAllIn(s).toSet


  //Send mail
  def sendMail(l: List[String]): Unit = {
    val mail = use[MailerPlugin].email
    mail.setSubject("Alerte Job")
    mail.setRecipient("Basile du Plessis <basile.duplessis@gmail.com>")
    mail.setFrom("Basile du Plessis <basile.duplessis@gmail.com>")

    val text = l.mkString(", ")

    mail.send( text, s"<html>$text</html>")
    Logger.info("Mail sent")

  }

  def doJobs: Unit = {
    withMongoConnection {
      Job.readAll
    } map {
      jobList => jobList foreach {
        job => withMongoConnection {doJob(job)} map {
          lastError => {
            Logger.debug("New ads saved")
          }
        }
      }
    } recover {
      case e => Logger.error("Error occurs while reading all jobs", e)
    }
  }

  /**
   * Execute job, send email with new ads
   * @param job
   * @return
   */
  def doJob(job: Job): Reader[DefaultDB, Future[LastError]] = {
    for {
      id <- pure(job.id.get.stringify)
      html <- pure(Source.fromURL(job.url)("iso-8859-15").getLines().mkString)
      ads <- pure(parseAds(html).toList)
      newAds <- pure(ads.filterNot(job.ads.get.toSet))
      sent <- pure(sendMail(newAds))
      saved <- JobService.updateAds(id, newAds)
    } yield saved
  }

}
