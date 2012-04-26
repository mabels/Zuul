package controllers;

import java.util.Collection;

import helpers.SpringUtils;
import play.mvc.Controller;
import services.Attendee;

public class WiFi extends Controller {

  public static void catchAll() {
    render();
  }

  public static void displayId() {
    final Collection<Attendee.Attendant.Ticket> result = SpringUtils
        .getInstance().getBean(Attendee.class).find(params.get("displayId"));
    if (result.size() > 0) {
      Attendee.Attendant.Ticket ticket = result.iterator().next();
      render(ticket);
    } else {
      render("WiFi/displayIdNotFound.html");
    }
  }

  public static class Login {
    public String host;
    public String remoteAddress;
    public String url;
    public String secure;
    public String code;
    public Boolean granted = false;
  }

  public static void askLogin(Login login) {
    System.err.println("WIFI:login");
    if (login.code != null &&

    login.code.equals("meno")) {
      login.granted = true;
      render("WiFi/loggedIn.html", login);
      return;
    }
    render(login);
  }

}
