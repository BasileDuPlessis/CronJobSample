package controllers

import com.typesafe.plugin._
import models.Job
import play.api._
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
import services.{MailService, JobService}

import scala.concurrent.Future

import utils.MongoConnection._

import play.api.libs.concurrent.Execution.Implicits.defaultContext


import play.api.Logger

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

    import scala.io.Source
    import play.api.Play.current

    val mailerAPI = use[MailerPlugin].email

    val executeJob = (job: Job) => for {
      source <- Future(Source.fromURL(job.url)("ISO-8859-15").getLines().mkString)
      matches <- Future("""(http://www.leboncoin.fr/ventes_immobilieres/[0-9]+\.htm)""".r.findAllIn(source).toList.toSet)
      lastError <- withMongoConnection {JobService.updateAds(job.id.get.stringify, matches)}
    } yield MailService.sendMail(
        matches.mkString(","),
        "Alerte Boncoin",
        List("basile.duplessis@gmail.com", "emmanuelle.ackermann@gmail.com"),
        "basile.duplessis@gmail.com"
      )(mailerAPI)


    withMongoConnection(Job.readAll) map {
      jobs => jobs foreach {
        job => executeJob(job) recover {
          case e => Logger.error("Error occurs while executing job: " + e.getMessage)
        }
      }
    } recover {
      case e => Logger.error("Error occurs while reading jobs: " + e.getMessage)
    }
  }

}
