package helpers;


import play.mvc.Mailer;
import services.Attendant;

public class Mails extends Mailer {
  public static void passPortCreated(Attendant.Ticket ticket, String passPort) {
    if (ticket.getEmail() == null) {
      return;
    }
    setFrom("WiFi <wifi@nextconf.eu>");
    setSubject("Your WiFi Accesscode");
    addRecipient(ticket.getEmail());
    send("Mails/passPortCreated.html", ticket, passPort);
  }
}
