package services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentOperationResult;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import services.PassPort.Ip2Mac;

/* This Class will create Pass Codes for gain
 * Access to the WiFi
 */

@Component
public class PassPorter extends CouchDbRepositorySupport<PassPort> implements
    InitializingBean {

  @Autowired
  public PassPorter(@Qualifier("passPorterDatabase") CouchDbConnector db) {
    super(PassPort.class, db);
    System.err.println("PassPorter construct");
    initStandardDesignDocument();
  }

  @GenerateView
  public List<PassPort> findByDisplayId(String id) {
    return queryView("by_displayId", id);
  }

  public interface FirstTime {
    public void run(PassPort pp);
  }
  @View(name = "getUnusedKeyCodes", map = "function(doc) { if (!doc.used) emit(doc._id, null) }")
  public PassPort createPass(String displayId, FirstTime firstTime) {
    List<PassPort> passPort = findByDisplayId(displayId);
    if (!passPort.isEmpty()) {
      return passPort.get(0);
    }
   
    while (true) {
      ViewQuery q = createQuery("getUnusedKeyCodes").includeDocs(true).limit(1);
      List<PassPort> codes = db.queryView(q, PassPort.class);
      if (!codes.isEmpty() && !codes.get(0).getUsed()) {
        codes.get(0).setUsed(true);
        codes.get(0).setDisplayId(displayId);
        if (firstTime != null) {
          firstTime.run(codes.get(0));
        }
        codes.get(0).setSendEmail(true);
        List<DocumentOperationResult> result = db.executeAllOrNothing(codes);
        if (result.isEmpty()) {
          return codes.get(0);
        }
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  
  @GenerateView
  @Override
  public List<PassPort> getAll() {
    ViewQuery q = createQuery("all").includeDocs(true);
    return db.queryView(q, PassPort.class);
  }
  
  @View(name = "getUnusedKeyCount", map = "function(doc) { if (!doc.used) emit(doc._id, null) }", reduce = "_count")
  public int getUnusedKeyCount() {
    ViewResult r = db.queryView(createQuery("getUnusedKeyCount"));
    List<Row> rows = r.getRows();
    if (rows.isEmpty()) {
      return 0;
    }
    return r.getRows().get(0).getValueAsInt();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    System.err.println("PassPorter init");
    new Thread(new Runnable() {

      @Override
      public void run() {
        while (true) {
          try {
            int result = getUnusedKeyCount();
            if (result < 50) {
              Set<String> codes = new HashSet<String>();
              String base = "23456789ABCDEFGHJKLMNPRSTUVWXYZabcdefghjklmnprstuvwxyz";
              for (int i = 0; i < 100; ++i) {
                long val = UUID.randomUUID().getLeastSignificantBits();
                String code = "";
                for (int j = 0; j < 64; ++j) {
                  int b = (int) (val & 0x1f); // 5bit
                  if (b < base.length()) {
                    code = code + base.charAt(b);
                  }
                  if (code.length() >= 5) {
                    codes.add(code);
                    System.err.println("DOIT:" + code);
                    break;
                  }
                  val = val >> 5;
                }
              }
              ViewQuery q = createQuery("all").keys(codes);
              List<PassPort> passports = db.queryView(q, PassPort.class);
              for (PassPort p : passports) {
                codes.remove(p.getPassPortId());
              }
              db.executeAllOrNothing(CollectionUtils.collect(codes,
                  new Transformer() {

                    @Override
                    public Object transform(Object arg0) {
                      PassPort p = new PassPort();
                      p.setId((String) arg0);
                      p.setUsed(false);
                      return p;
                    }
                  }));
            }

            Thread.sleep(10000);
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

      }
    }).start();

  }
  
  
  public String assignClient(PassPort pp, String ip, String mac) {
    if (ip == null) {
      return "no ip found";
    }
    if (mac == null) {
      return "no mac found";
    }
    if (pp.getClients() == null) {
      pp.setClients(new ArrayList<PassPort.Ip2Mac>(3));
    }
    for(Ip2Mac im : pp.getClients()) {
      if (im.getIp().equals(ip) && im.getMac().equals(mac)) {
        return "granted";
      }
    }
    if (pp.getClients().size() >= 3) {
      return "too many clients";
    }
    Ip2Mac i2m = new Ip2Mac();
    i2m.setIp(ip);
    i2m.setMac(mac);
    pp.getClients().add(i2m);
    db.update(pp);
    return "granted";
  }


}
