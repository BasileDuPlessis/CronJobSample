import java.io.InputStream

import org.specs2.mutable.After
import org.specs2.specification.Step
import org.subethamail.smtp.helper.{SimpleMessageListenerAdapter, SimpleMessageListener}
import org.subethamail.smtp.server.SMTPServer
import play.api.test.FakeApplication

import scala.concurrent.Promise
import scala.io.Source
import scala.util.Try

class EmbedSMTPContext extends After {

  val additionalConfiguration: Map[String, String] = Map()
  val withoutPlugins: Seq[String] = Seq()

  val promiseMessage = Promise[String]
  val lastReceivedMessage = promiseMessage.future

  val messageListener = new SimpleMessageListener {
    def accept(from: String, recipient: String): Boolean = true
    def deliver(from: String, recipient: String, data: InputStream): Unit = {
      if (!promiseMessage.isCompleted) promiseMessage.success(Source.fromInputStream(data).mkString)
    }
  }

  val smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageListener))

  def startSMTPServer: Unit = {
    Try {smtpServer.start()} recover {
      case e:Exception => smtpServer.setPort(smtpServer.getPort + 1); startSMTPServer
    }
  }

  startSMTPServer

  lazy val fakeApp = FakeApplication(
    additionalConfiguration = Map("smtp.host" -> smtpServer.getHostName, "smtp.port" -> smtpServer.getPort) ++ additionalConfiguration,
    withoutPlugins = withoutPlugins
  )

  def after = {
    Step(smtpServer.stop())
  }

}