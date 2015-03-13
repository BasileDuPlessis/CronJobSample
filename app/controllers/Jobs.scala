package controllers

import com.typesafe.plugin._
import libraries.Di._
import models.Job
import play.api._
import play.api.mvc._
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import services.{MailService, JobService}
import java.net.URL

import scala.concurrent.Future

import utils.MongoConnection._

import play.api.libs.concurrent.Execution.Implicits.defaultContext


import play.api.Logger

import scala.io.Source

/**
 * Controller for Job tasks
 */
object Jobs extends Controller {

  def index = Action {
    Ok(views.html.jobs.create(Job.jobForm))
  }

  def view(id: String) = Action.async {
    withMongoConnection {
      JobService.readFromId(id)
    } map {
      case Some(job: Job) => Ok(views.html.jobs.view(job))
      case _ => NotFound(s"Job $id not found")
    } recover {
      case e => BadRequest(e.getMessage)
    }
  }

  def create = Action.async { implicit request =>
    Job.jobForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.jobs.create(formWithErrors))),
      job => {
        val id = BSONObjectID.generate
        withMongoConnection {
          Job.insert(job.copy(id = Some(id)))
        } map {
          lastError => {
            Logger.debug(s"Job $id created")
            Redirect(routes.Jobs.view(id.stringify))
          }
        } recover {
          case e => BadRequest(e.getMessage)
        }
      }
    )
  }

  def executeAllJobs = {

    import play.api.Play.current

    val mailerAPI = use[MailerPlugin].email

    withMongoConnection(Job.readAll) map {
      jobs => jobs foreach {
        job => {
          val url = new URL(job.url)
          val host = url.getHost
          val ads = job.pattern.r.findAllIn(Source.fromURL(url)("ISO-8859-15").getLines().mkString).toSet
          
          val absAds = ads map { ad => 
            ad match {
              case s:String if s._head == "/" => host + _
              case _ => _
            }
          }
          
          val diff = absAds.filterNot(job.ads.get.toSet)

          if (diff.size > 0) {
            withMongoConnection(JobService.updateAds(job.id.get.stringify, diff)) map {
              lastError => Logger.info("Ads saved with: " + lastError)
            } recover {
              case e => Logger.error("Unable to save ads: " + e.getMessage)
            }

            MailService.sendMail(
              views.html.mail.alert.render(diff).body,
              "Alerte email",
              List("basile.duplessis@gmail.com", "emmanuelle.ackermann@gmail.com"),
              "basile.duplessis@gmail.com"
            )(mailerAPI)
          }
        }
      }
    } recover {
      case e => Logger.error("Unable to read all jobs: " + e.getMessage)
    }

  }

  def migrationAddPattern(p: String) = Action.async {
    withMongoConnection{
      Job.addDefaultPatternToUndefinedPatternFields(p)
    } map {
      lastError => Ok("done")
    } recover {
      case e => BadRequest(e.getMessage)
    }
  }
}
