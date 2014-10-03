package controllers

import play.api._
import play.api.mvc._
import models.Job
import utils.MongoConnection._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  def index = Action.async {
    withMongoConnection {
      Job.readAll
    } map {
      j => Ok(views.html.index(j))
    } recover {
      case e => InternalServerError
    }
  }

}