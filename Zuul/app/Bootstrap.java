import helpers.SpringUtils;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import services.PassPorter;

@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
      //@SuppressWarnings("unused")
      //final PassPorter passPorter = SpringUtils.getInstance().getBean(PassPorter.class);
      PassPorter obj = SpringUtils.getInstance().getBean(PassPorter.class);
      System.err.println("BOOT:"+obj);
      //obj.getAll();
      //BlogPostRepository bpr = SpringUtils.getInstance().getBean(BlogPostRepository.class);
      //bpr.getAll();
    }    
}