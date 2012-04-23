package controllers;

import helpers.SpringUtils;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import services.Attendee;
import views.html.index;
import views.html.loggedin;

public class Login extends Controller {

  public static class Code {
    @Required public String code;
    @Required public String path;
    @Required public String host;
  }
  
  public static Result catchAll() {
    final Form<Code> code = form(Code.class);
    code.data().put("path", "kkk");
    
    code.data().put("host", request().getHeader("Host"));
    return ok(
      index.render(code)
    );
  }

  public static Result index(String path) {
    //Logger.info("XXXXXX"+SpringUtils.getInstance().getBean(Attendee.class).find("meno"));
    
    final Form<Code> code = form(Code.class);
    code.data().put("path", path);
    code.data().put("host", request().getHeader("Host"));
    return ok(
      index.render(code)
    );
  }

  public static Result check() {
    Form<Code> form = form(Code.class).bindFromRequest();
    if(form.hasErrors()) {
      return badRequest(index.render(form));
    } else {
      Code data = form.get();
      return ok(
        loggedin.render(form.get())
      );
    }
  }

}
