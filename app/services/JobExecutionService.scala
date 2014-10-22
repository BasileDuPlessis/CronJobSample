package services

import com.typesafe.plugin.MailerAPI
import libraries.Di._
import models.Job
import reactivemongo.api.DefaultDB

import scala.concurrent.Future
import scala.io.Source

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Job execution
 */
object JobExecutionService {

  def executeAll: Reader[DefaultDB, Future[List[Reader[DefaultDB, (MailerAPI) => Unit]]]] = for {
    jobs <- Job.readAll
    result <- pure(jobs.map(l => l map (
      (job:Job) => for {
        m <- pure("""(http://www.leboncoin.fr/ventes_immobilieres/[0-9]+\.htm)""".r.findAllIn(Source.fromURL(job.url)("ISO-8859-15").getLines().mkString).toList.toSet)
        r <- JobService.updateAds(job.id.get.stringify, m)
      } yield MailService.sendMail(
          m.mkString(","),
          "Alerte Boncoin",
          List("basile.duplessis@gmail.com", "emmanuelle.ackermann@gmail.com"),
          "basile.duplessis@gmail.com"
        )
      )))
  } yield result
  
}
