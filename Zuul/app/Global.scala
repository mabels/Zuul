import play.api._

import play.api.mvc._
import play.api.mvc.Results._

import play.api.data._
import play.api.data.Forms._


object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Application has started")
  }  
  
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }  

override def onHandlerNotFound(request: RequestHeader): Result = {    

	val code = Form(
		"path" -> request.path,
		"host" -> request.getHeader("Host")
	)
    Ok(views.html.index(), code)
  }
}