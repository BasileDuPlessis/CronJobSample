import java.io.InputStream

import org.specs2.main.Arguments
import org.specs2.specification._
import org.specs2.matcher._
import org.specs2.mutable.SpecificationLike

import org.subethamail.smtp.helper.{SimpleMessageListenerAdapter, SimpleMessageListener}
import org.subethamail.smtp.server.SMTPServer
import play.api.Logger

import scala.io.Source
import scala.concurrent.{Promise, Future}

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Add SMTP server to specification
 */
trait EmbedSMTPServer extends FragmentsBuilder {
  self: SpecificationLike =>

  var promiseMessage = Promise[String]
  var lastReceivedMessage = promiseMessage.future

  lazy val messageListener = new SimpleMessageListener {
    def accept(from: String, recipient: String): Boolean = true
    def deliver(from: String, recipient: String, data: InputStream): Unit = {
      if (promiseMessage.isCompleted) {
        promiseMessage = Promise[String]
        lastReceivedMessage = promiseMessage.future
      }
      promiseMessage.success(Source.fromInputStream(data).mkString)
    }
  }

  lazy val smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageListener))

  private def startSMTP() = Example("Start SMTP Server", {smtpServer.start(); success})
  private def stopSMTP() = Example("Stop SMTP Server", {smtpServer.stop(); success})

  override def map(fs: => Fragments) = startSMTP ^ fs ^ stopSMTP

}
