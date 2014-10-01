package services

import java.net.URL

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

  //Get Html from url
  def getHtml(url: String): String = {
    Logger.info("Get html from url: " + url)

    val conn = new URL(url).openConnection()

    Logger.info("HTML length: " + conn.getContentLength)

    ""
  }

  //Get Ads from an url
  def parseAds(s: String): Set[String] = {
    Logger.info("Parse ads in string")
    val ads = """(http://www.leboncoin.fr/ventes_immobilieres/[0-9]+\.htm)""".r.findAllIn(s).toSet
    Logger.info("Ads number: " + ads.size)
    ads
  }


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

  def getNewAds(a: List[String], b: List[String]): List[String] = a.filterNot(b.toSet)


  def doJobs: Unit = {
    withMongoConnection {
      Job.readAll
    } map {
      jobList => jobList foreach {
        job => withMongoConnection {doJob(job)} map {
          lastError => {
            Logger.info("New ads saved with lastError: " + lastError)
          }
        } recover {
          case e => Logger.info("No ads to save: " + e.getMessage)
        }
      }
    } recover {
      case e => Logger.error("Error occurs while reading all jobs: " + e.getMessage)
    }
  }

  /**
   * Execute job, send email with new ads
   * @param job
   * @return
   */
  def doJob(job: Job): Reader[DefaultDB, Future[LastError]] = {
    for {
      id <- Future(job.id.get)
      ads <- Future(getNewAds(parseAds(getHtml(job.url)).toList, job.ads.get))
      if (ads.length > 0)
    } yield {
      sendMail(ads)
      JobService.updateAds(id.stringify, ads)
    }
  }

}
