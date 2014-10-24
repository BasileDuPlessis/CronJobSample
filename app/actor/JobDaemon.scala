package actor


import akka.actor.Actor
import controllers.Jobs
import play.api.Logger


/**
 * Created by basile.duplessis on 12/09/2014.
 */
class JobDaemon extends Actor {

  def receive = {
    case "jobActor" => {
      Logger.info("Looking for jobs to execute")
      //Jobs.executeAllJobs
    }
  }

}
