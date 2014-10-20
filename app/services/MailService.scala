package services

import com.typesafe.plugin._
import play.api.Logger

import play.api.Play.current

/**
 * Created by bduplessis on 06/10/2014.
 */
object MailService {
 //"Basile du Plessis <basile.duplessis@gmail.com>", "Emmanuelle Ackermann <emmanuelle.ackermann@gmail.com>"
  //Send mail
  def sendMail(message: String, subject: String, recipient: List[String], from: String) = {
    (mailerAPI: MailerAPI) => {
      mailerAPI.setSubject(subject)
      mailerAPI.setRecipient(recipient:_*)
      mailerAPI.setFrom(from)
      mailerAPI.send(message, s"<html>$message</html>")
    }
  }

}
