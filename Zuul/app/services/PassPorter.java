package services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.ektorp.CouchDbConnector;
import org.ektorp.DbInfo;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.DocumentOperationResult;
import org.ektorp.Options;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
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

    play.Logger.info("PassPorter construct");
    initStandardDesignDocument();
  }

  //
  // @GenerateView
  // public List<PassPort> findByDisplayId(String id) {
  // return queryView("by_displayId", id);
  // }

  public interface FirstTime {
    public void run(PassPort pp);
  }

  // @View(name = "getUnusedKeyCodes", map =
  // "function(doc) { if (!doc.used) emit(doc._id, null) }")
  public PassPort createPass(String displayId, int maxClients,
      FirstTime firstTime) {
    try {
      return this.get(displayId);
    } catch (DocumentNotFoundException e) {
      PassPort pass = new PassPort();
      pass.setId(displayId);
      pass.setUsed(true);
      pass.setDisplayId(displayId);
      pass.setMaxClients(maxClients);
      db.create(pass);
      firstTime.run(pass);
      return pass;
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
    play.Logger.info("PassPorter init");
    feedChanges();
    // new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // while (true) {
    // try {
    // int result = getUnusedKeyCount();
    // if (result < 500) {
    // Set<String> codes = new HashSet<String>();
    // String base = "23456789ABCDEFGHJKLMNPRSTUVWXYZabcdefghjklmnprstuvwxyz";
    // for (int i = 0; i < 10000; ++i) {
    // long val = UUID.randomUUID().getLeastSignificantBits();
    // String code = "";
    // for (int j = 0; j < 64; ++j) {
    // int b = (int) (val & 0x1f); // 5bit
    // if (b < base.length()) {
    // code = code + base.charAt(b);
    // }
    // if (code.length() >= 5) {
    // codes.add(code);
    // play.Logger.debug("DOIT:" + code);
    // break;
    // }
    // val = val >> 5;
    // }
    // }
    // ViewQuery q = createQuery("all").keys(codes);
    // List<PassPort> passports = db.queryView(q, PassPort.class);
    // for (PassPort p : passports) {
    // codes.remove(p.getPassPortId());
    // }
    // db.executeAllOrNothing(CollectionUtils.collect(codes,
    // new Transformer() {
    //
    // @Override
    // public Object transform(Object arg0) {
    // PassPort p = new PassPort();
    // p.setId((String) arg0);
    // p.setUsed(false);
    // return p;
    // }
    // }));
    // }
    //
    // Thread.sleep(10000);
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    //
    // }
    // }).start();

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
    boolean found = false;
    for (Ip2Mac im : pp.getClients()) {
      if (im.getIp().equals(ip) && im.getMac().equals(mac)) {
        play.Logger.info("ip2mac is registered");
        im.setPid(0);
        found = true;
      }
    }
    if (found) {
      db.update(pp);
      return "granted";
    }
    if (pp.getClients().size() >= pp.getMaxClients()) {
      return "too many clients";
    }
    Ip2Mac i2m = new Ip2Mac();
    i2m.setIp(ip);
    i2m.setMac(mac);
    pp.getClients().add(i2m);
    db.update(pp);
    play.Logger.info("ip2mac store");
    return "granted";
  }

  private void processor(final BlockingQueue<Runnable> q) {
    (new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          while (true) {
            Runnable run;
            run = q.take();
            if (run != null) {
              run.run();
            } else {
              play.Logger.info("BlockingQueue return null");
            }
          }
        } catch (InterruptedException e) {
          play.Logger.info("processor: q.take got abort", e);
        }
      }
    })).start();
  }

  public void reloadMacs() {
    PassPort.nextTransactionId(); // force reload
    initialLoader(q);
  }

  private void initialLoader(final BlockingQueue<Runnable> q) {
    synchronized (q) {
      final PassPorter my = this;
      (new Thread(new Runnable() {

        @Override
        public void run() {
          // PassPort.Ip2Mac.clearIPTables();
          q.addAll(CollectionUtils.collect(my.getAll(), new Transformer() {

            @Override
            public Object transform(Object arg0) {
              final String pp = ((PassPort) arg0).getId();
              return new Runnable() {

                @Override
                public void run() {
                  for (int i = 0; i < 10; ++i) {
                    try {
                      PassPort my = db.get(PassPort.class, pp);
                      play.Logger.info("initialLoader:" + my.getDisplayId());
                      if (my.openFireWall(false)) {
                        try {
                          /*
                           * if (play.Play.configuration.get("application.mode")
                           * .equals("prod")) {
                           */
                          db.update(my);
                          // }
                          play.Logger.info("openFireWall:done:" + my.getId()
                              + ":" + i);
                          return;
                        } catch (org.ektorp.UpdateConflictException e) {
                          play.Logger.info("Retry openfirewall:for:"
                              + my.getId());
                        }
                      } else {
                        // play.Logger.info("no change" + my.getId());
                        return;
                      }
                    } catch (Throwable e) {
                        play.Logger.error("openFireWall failed:" + pp + ":" + e);
                        return;
                    } 
                  }
                  play.Logger.error("openFireWall to  much" + pp);
                  return;
                }
              };
            }
          }));

        }
      })).start();
    }
  }

  // public BlockingQueue<Runnable> getQ() {
  // return q;
  // }
  // private final Set<String> blockIds = new Conc<String>();
  private final BlockingQueue<Runnable> q = new LinkedBlockingQueue<Runnable>();

  private void feedChanges() {
    final PassPorter my = this;
    (new Thread(new Runnable() {
      @Override
      public void run() {
        play.Logger.info("started feedChanges" + db.getDatabaseName());
        initialLoader(q);
        processor(q);
        processor(q);
        while(true) {
          try {
            long seq = db.getDbInfo().getUpdateSeq();
            ChangesCommand cmd = new ChangesCommand.Builder().since(seq).heartbeat(10).build();
            ChangesFeed feed = db.changesFeed(cmd);
            while (feed.isAlive()) {
              try {
                final DocumentChange dc = feed.next();
                play.Logger.info("DocumentChange:"+dc.getId()+":"+dc.getRevision()+":"+dc.getSequence());
                q.add(new Runnable() {
                  @Override
                  public void run() {
                    try {
                      if (dc.isDeleted()) {
                        play.Logger.info("**** isDeleted():" + dc.getId() + ":"
                            + dc.getRevision());
                        Options op = new Options();
                        op.revision(dc.getRevision());
                        PassPort pp = db.get(PassPort.class, dc.getId(), op);
                        return;
                      }
                      PassPort pp = my.get(dc.getId());
                      play.Logger.info("feedChanges:"+pp.getDisplayId());
                      if (pp.openFireWall(true)) {
                    	play.Logger.info("openFireWall:mode:"+play.Play.configuration.get("application.mode"));
                        if (play.Play.configuration.get("application.mode").equals(
                            "prod")) {
                         play.Logger.info("db:Update:pre:"+dc.getId()+":"+dc.getRevision());
                         db.update(pp);
                         play.Logger.info("db:Update:pst:"+dc.getId()+":"+dc.getRevision());   
                       }
                      }
                    } catch (Throwable e) {
                      play.Logger.error("update feed aborted:"+e.getMessage());
                    } 
                  }
                });
              } catch (InterruptedException e) {
                play.Logger.error("changes feed aborted:"+e.getMessage());
              }
          }
        } catch (Exception e) {
          play.Logger.error("update feed aborted:"+e.getMessage());        
        }
      }
    }
    })).start();
  }
}
