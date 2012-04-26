package controllers;

import java.util.Collection;

import helpers.SpringUtils;
import play.Logger;
import play.mvc.Controller;
import services.Attendee;

public class Admin extends Controller {

  public static void service() {
    Collection<Attendee.Attendant.Ticket> tickets = SpringUtils.getInstance()
        .getBean(Attendee.class).find(params.get("q"));
    render(tickets);
  }

  public static void search() {
    render();
  }

  public static void print() {
    render();
  }

  public static void formAttendant() {
    render();
  }

  public static void newAttendant(Attendee.Attendant.Ticket ticket) {
    Attendee.Attendant attendant = new Attendee.Attendant();
    attendant.setTicket(ticket);
    Logger.info("TICKET "+ ticket.isValid());
    attendant.setSuccess(ticket.isValid());
    renderJSON(attendant);
  }

}
