package actor


import akka.actor.Actor
import play.api.Logger


/**
 * Created by basile.duplessis on 12/09/2014.
 */
class JobDaemon extends Actor {

  def receive = {
    case _ => {
      Logger.info("Looking for jobs to execute")
    }
  }

}
