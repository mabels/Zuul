import helpers.SpringUtils;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import services.PassPorter;

@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
      PassPorter obj = SpringUtils.getInstance().getBean(PassPorter.class);
      play.Logger.info("BOOT:"+obj);
      obj.findByDisplayId("-");
   }    
}
