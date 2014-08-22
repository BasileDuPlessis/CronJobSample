package controllers

import play.api._
import play.api.mvc._

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Controller for Job tasks
 */
object Jobs extends Controller {

  def create = Action.async { implicit request => {
      Future(NotFound("not implemented"))
    }
  }

}
