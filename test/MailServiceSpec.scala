
import java.io.InputStream

import org.specs2.execute.{Result, AsResult}
import org.subethamail.smtp.helper.{SimpleMessageListenerAdapter, SimpleMessageListener}
import org.subethamail.smtp.server.SMTPServer
import play.api.test._
import play.api.test.Helpers._
import services.MailService

import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import play.api.Logger

/**
 * Specs for MailService
 */
class MailServiceSpec extends Specification with EmbedSMTPServer {
  sequential

  val fakeApp = FakeApplication(
    additionalConfiguration = Map("smtp.host" -> smtpServer.getHostName),
    withoutPlugins = Seq("play.modules.reactivemongo.ReactiveMongoPlugin")
  )
/*
  class withSMTP(app: FakeApplication) extends WithApplication(app) {
    lazy val messageListener = new SimpleMessageListener {
      def accept(from: String, recipient: String): Boolean = true
      def deliver(from: String, recipient: String, data: InputStream): Unit = {
        Logger.info(from)
      }
    }
    lazy val smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageListener))
    override def around[T: AsResult](t: => T): Result = {
      smtpServer.start()
      val result = super.around(t)
      smtpServer.stop()
      result
    }
  }
  */

  "MailService#sendMail" should {
    "send email" in running(fakeApp) {

      val message = "Hello World, this is the MailService !"

      MailService.sendMail(message)

      Await.result(lastReceivedMessage, Duration.Inf) must contain(message)

    }
  }


}
