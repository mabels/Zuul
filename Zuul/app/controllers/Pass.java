package controllers;

import helpers.Mails;
import helpers.ResolvArp;
import helpers.SpringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ektorp.DocumentNotFoundException;

import play.libs.IO;
import play.mvc.Controller;
import services.Attendant;
import services.Attendant.Ticket;
import services.Attendants;
import services.PassPort;
import services.PassPorter;

import com.google.gson.Gson;

public class Pass extends Controller {

  public static void app() {
    play.Logger.info("WIFI:app");
    render();
  }

  public static void longDisplayId(String displayId) {
    // FALSCH
    
    String code = Pass.tryLogin(displayId.trim());
    play.Logger.info("tryLogin:return="+ code);
    if (code.startsWith("http")) {
      redirect(code);
    } else if (code.startsWith("granted")) {
      redirect(play.Play.configuration.get("application.baseUrl")+"/grant/"+displayId);
    }
    redirect(play.Play.configuration.get("application.baseUrl")+"/WiFi/askLogin");
  }

  public static void displayId(String displayId) {
    play.Logger.info("Pass:displayId" + params.get("displayId"));

    Attendant attendant = null;
    try {
      attendant = SpringUtils.getInstance().getBean(Attendants.class)
          .get(Attendant.longId(params.get("displayId")));
    } catch (DocumentNotFoundException e) {
      render("WiFi/displayIdNotFound.html");
      return;
    }
    final Ticket ticket = attendant.getTicket();
    try {
      final PassPort passPort = SpringUtils.getInstance()
          .getBean(PassPorter.class).get(attendant.getId());
      passPort.setBaseUrl(play.Play.configuration
          .getProperty("application.baseUrl"));
      render(ticket, passPort);
    } catch (DocumentNotFoundException e) {
      play.Logger.info("SelfRegister Pass:displayId" + params.get("displayId"));
      redirect(play.Play.configuration.get("application.baseUrl")
          + "WiFi/Pass/create/" + params.get("displayId"));
      return;
    }
  }
  
  private static class FirstTime implements PassPorter.FirstTime {
    Ticket ticket;
    String id;
    public FirstTime(Ticket ticket, String id) {
      this.ticket = ticket;
      this.id = id;
    }
    public void run(PassPort pp) {
      Mails.passPortCreated(ticket, id);
    }
    
  }

  public static void create() {
    play.Logger.info("***********WIFI:create" + params.get("displayId"));
    Attendants attendants = SpringUtils.getInstance().getBean(Attendants.class);
    final Attendant attendant = attendants.get(Attendant.longId(params
        .get("displayId")));
    if (attendant == null) {
      render("createError.html");
      return;
    }
    final PassPort passPort = SpringUtils.getInstance()
        .getBean(PassPorter.class)
        .createPass(attendant.getId(), 3, new FirstTime(attendant.getTicket(), attendant.getId()));
    if (passPort == null) {
      render("createError.html");
      return;
    }
    redirect(play.Play.configuration.get("application.baseUrl")
        + attendant.getTicket().getShortDisplayIdentifier());
  }

  public static void print() throws Exception {
    play.Logger.info("WIFI:print:" + params.get("displayId") + ":"
        + params.get("printer"));
    Attendants attendants = SpringUtils.getInstance().getBean(Attendants.class);
    final Attendant attendant = attendants.get(Attendant.longId(params
        .get("displayId")));
    if (attendant == null) {
      render("createError.html");
      return;
    }
    final PassPort passPort = SpringUtils.getInstance()
        .getBean(PassPorter.class)
        .createPass(attendant.getId(), 3, new FirstTime(attendant.getTicket(), attendant.getId()));
    if (passPort == null) {
      render("createError.html");
      return;
    }
    // -f FIRSTNAME -s SECONDNAME -l LASTNAME -c COMPANYNAME -a CATEGORY -q
    // bild.png
    // Runtime.getRuntime().exec("../)" +

    FileOutputStream fos = new FileOutputStream(
        play.Play.configuration.get("play.tmp") + "/" + passPort.getId()
            + ".png");
    IO.copy(WiFi.makeQrCode(passPort.getId(), 200), fos);
    fos.close();

    String printer = params.get("printer");
    if (printer == null) {
      printer = "QR-Acc-01";
    }

    String[] args = new String[] {
        "wifi_code",
        "-f",
        StringUtils.defaultString(attendant.getTicket().getFirstName()),
        "-l",
        StringUtils.defaultString(attendant.getTicket().getLastName()),
        "-q",
        play.Play.configuration.get("play.tmp") + "/" + passPort.getId()
            + ".png", "-p", printer };

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
    play.Logger.info("Pass:print:cmd:" + StringUtils.join(sb.toArray(), ":"));
    pb.command(sb);
    pb.directory(new File(play.Play.configuration.get("zuul.base")
        + "/wifi_code"));
    Process p = pb.start();
    // IO.copy(p.getInputStream(), System.err);
    // p.wait(10000);
    // p.destroy();
    render();
  }

  public static String tryLogin(String attendantId) {
    play.Logger.info("tryLogin to=" + attendantId);
    Attendant attendant;
    try {
      final Attendants attendants = SpringUtils.getInstance().getBean(
          Attendants.class);
      // Test Existenz
      attendant = attendants.get(attendantId);
      play.Logger.info("found id:" + attendantId);
      // redirect(play.Play.configuration.get("application.baseUrl")+tickets.iterator().next().toString());
      // return play.Play.configuration.get("application.baseUrl")
      // + attendant.getTicket().getShortDisplayIdentifier();
    } catch (DocumentNotFoundException e) {
      return "code not found";
    } catch (IllegalArgumentException e) {
      return "code not found(id not found)";
    }

    /* Test Code */
    WiFi.Login login = new WiFi.Login(request);
    if (login.remoteAddress.equals("127.0.0.1")
        || login.remoteAddress.equals("0:0:0:0:0:0:0:1")) {
      login.remoteAddress = "192.168.176.113";
    }
    final PassPorter passPorter = SpringUtils.getInstance().getBean(
        PassPorter.class);
    final PassPort passPort = passPorter.createPass(attendantId, 3, new FirstTime(attendant.getTicket(), attendantId));
    return passPorter.assignClient(passPort, login.remoteAddress,
        ResolvArp.ip2mac(login.remoteAddress));
  }

  public static void grantAccess() {
    String passPortId = params.get("passPortId");
    render(passPortId);
  }

  public static void grantAccessJsonp() {
    String passPortId = params.get("passPortId");
    String code = tryLogin(passPortId);
    String json = new Gson().toJson(code);
    render(json);
  }

  public static void reloadMacs() {
    SpringUtils.getInstance().getBean(PassPorter.class).reloadMacs();
    render();
  }
  /*
   * 
   * final Collection<Attendee.Attendant.Ticket> result = SpringUtils
   * .getInstance().getBean(Attendee.class).find(params.get("displayId")); if
   * (result.size() > 0) { Attendee.Attendant.Ticket ticket =
   * result.iterator().next(); render(ticket); } else {
   * render("WiFi/displayIdNotFound.html"); }
   * 
   * 
   * public static void getPass(String displayId) {
   * SpringUtils.getInstance().getBean
   * (Attendee.class).find(params.get("displayId"));
   * 
   * //List<PassPort> ret =
   * SpringUtils.getInstance().getBean(PassPorter.class).useCodes(1); }
   */

}
