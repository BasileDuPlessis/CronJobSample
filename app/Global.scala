import scala.concurrent.duration.DurationInt
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import akka.actor.Props
import actor.JobDaemon

/**
 * Global
 */
object Global extends GlobalSettings {

  override def onStart(app: Application) {
    play.api.Play.mode(app) match {
      case play.api.Mode.Test => // do not schedule anything for Test
      case _ => reminderDaemon(app)
    }
  }

  def reminderDaemon(app: Application) = {
    Logger.info("Scheduling the job daemon")
    val jobActor = Akka.system(app).actorOf(Props(new JobDaemon()))
    Akka.system(app).scheduler.schedule(0 seconds, 5 minutes, jobActor, "jobActor")
    Akka.system(app).scheduler.schedule(0 seconds, 30 minutes, jobActor, "cloudBeesActivator")
  }

}
