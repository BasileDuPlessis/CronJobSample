package services

import com.typesafe.plugin._
import play.api.Logger

import play.api.Play.current

/**
 * Created by bduplessis on 06/10/2014.
 */
object MailService {

  //Send mail
  def sendMail(m: String): Unit = {
    val mail = use[MailerPlugin].email
    mail.setSubject("Alerte Annonce")
    mail.setRecipient("Basile du Plessis <basile.duplessis@gmail.com>", "Emmanuelle Ackermann <emmanuelle.ackermann@gmail.com>")
    mail.setFrom("Basile du Plessis <basile.duplessis@gmail.com>")
    mail.send( m, s"<html>$m</html>")
    Logger.info("Mail sent")

  }

}
