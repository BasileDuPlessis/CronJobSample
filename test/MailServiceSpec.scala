import com.typesafe.plugin._
import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._
import services.MailService

import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * Specs for MailService
 */
class MailServiceSpec extends Specification {

  "MailService#sendMail" should {
    "send email" in new EmbedSMTPContext {

      override val withoutPlugins = Seq("play.modules.reactivemongo.ReactiveMongoPlugin")

      running(fakeApp) {

        val message = "Hello World, this is the MailService !"

        MailService.sendMail(message, "Subject", List("test@test.com"), "test@test.com")(use[MailerPlugin].email)

        Await.result(lastReceivedMessage, Duration.Inf) must contain(message)

      }
    }
  }

}
