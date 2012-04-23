package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import play.data.validation.Constraints.*;
import java.util.*;

import views.html.*;

public class Login extends Controller {

  public static class Code {
    @Required public String code;
    @Required public String path;
    @Required public String host;
  }

  public static Result index(String path) {
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
