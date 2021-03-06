package controllers;

import helpers.SpringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.libs.IO;
import play.mvc.Controller;
import play.mvc.With;
import services.Attendant;
import services.Attendants;


//@With(Security.class)
public class Admin extends Controller {

  public static void service() {
    Collection<Attendant.Ticket> tickets = SpringUtils.getInstance()
        .getBean(Attendants.class).find(params.get("q"));
    render(tickets);
  }

  public static void search() {
    render();
  }

  public static void formAttendant() {
    render();
  }

  public static void newAttendant(Attendant.Ticket ticket) {
    Attendant attendant = new Attendant();
    attendant.setTicket(ticket);
    Logger.info("TICKET "+ ticket.isValid());
    if (ticket.isValid()) {
      attendant.setSuccess(ticket.isValid());
      SpringUtils.getInstance().getBean(Attendants.class).createAttendant(attendant);
    }
    renderJSON(attendant);
  }
  
  public static void printBadge() throws Exception {
    play.Logger.info("Admin:print:" + params.get("displayId"));
    
    final Attendant attendant = SpringUtils.getInstance()
        .getBean(Attendants.class)
        .get(params.get("displayId"));
    if (attendant == null) {
      render("createError.html");
      return;
    }
    FileOutputStream fos = new FileOutputStream(play.Play.configuration.get("play.tmp")+ "/" + attendant.getId() + ".png");
    IO.copy(WiFi.makeQrCode(attendant.getTicket().getShortDisplayIdentifier(), 300), fos);
    fos.close();

    String o = params.get("printer");
    if (o == null || o.isEmpty()) {
      o = "BADGES-D4";
    }

    String[] args = new String[] { "/usr/bin/ruby", "badge", "-f",
        StringUtils.defaultString(attendant.getTicket().getFirstName()), "-l",
        StringUtils.defaultString(attendant.getTicket().getLastName()), "-q",
        play.Play.configuration.get("play.tmp")+ "/" + attendant.getId() + ".png",
        "-p", o };

    ProcessBuilder pb = new ProcessBuilder();
    List<String> sb = new ArrayList<String>(args.length);
    for (String i : args) {
      sb.add(i);
    }
    String company = attendant.getTicket().getCompany();
    if (company != null && !company.isEmpty()) {
      sb.add("-c");
      sb.add(company);
    }
    play.Logger.info("Admin:print:cmd:" + StringUtils.join(sb.toArray(), ":"));
    pb.command(sb);
    pb.directory(new File(play.Play.configuration.get("zuul.base")+"/badge"));
    Process p = pb.start();
    //IO.copy(p.getInputStream(), System.err);
    // p.wait(10000);
    // p.destroy();
    render();
  }


}
