package actor


import akka.actor.Actor
import play.api.Logger
import services.BoncoinService
import scala.io.Source


/**
 * Created by basile.duplessis on 12/09/2014.
 */
class JobDaemon extends Actor {

  def receive = {
    case "jobActor" => {
      Logger.info("Looking for jobs to execute")
      BoncoinService.doJobs
    }
    case "cloudBeesActivator" => {
      Logger.info("Call cloudbees home")
      Source.fromURL("http://cronjobsample.basileduplessis.eu.cloudbees.net/")
    }
  }

}
