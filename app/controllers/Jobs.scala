package controllers

import models.Job
import play.api._
import play.api.mvc._
import reactivemongo.bson.BSONObjectID

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

  def view(id: String) = Action {
    Ok(id)
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

}
