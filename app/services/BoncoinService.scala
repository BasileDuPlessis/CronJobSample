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
    mail.setSubject("Alerte Boncoin")
    mail.setRecipient("Basile du Plessis <basile.duplessis@gmail.com>")
    mail.setFrom("Basile du Plessis <basile.duplessis@gmail.com>")

    val text = l.mkString(", ")

    mail.send( text, s"<html>$text</html>")

  }

  def doJobs: Unit = {
    withMongoConnection {
      Job.readAll
    } map {
      jobList => jobList foreach {
        job => withMongoConnection {doJob(job)}
      }
    } recover {
      case e => Logger.error("Error occurs while reading all jobs", e)
    }
  }

  def doJob(job: Job): Reader[DefaultDB, Future[LastError]] = {
    for {
      html <- pure(Source.fromURL(job.url).getLines().mkString)
      adsList <- pure(parseAds(html).toList)
      //newAdsList <- adsList.filterNot(Set[String]())
      sent <- pure(sendMail(adsList))
      saved <- Job.updateAds(adsList)
    } yield saved
  }

}
