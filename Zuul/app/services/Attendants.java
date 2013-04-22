package services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.ektorp.CouchDbConnector;
import org.ektorp.UpdateConflictException;
import org.ektorp.ViewQuery;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Attendants extends CouchDbRepositorySupport<Attendant> {

  @Autowired
  public Attendants(@Qualifier("attendantsDatabase") CouchDbConnector db) {
    super(Attendant.class, db);
    play.Logger.info("Attendant construct");
    initStandardDesignDocument();
  }

  /*
  @View(name = "findByEmail", map = "function(doc) { doc.ticket.email && emit(doc.ticket.email.toLowerCase(), null); }")
  public Collection<Attendant.Ticket> findByEmail(String str) {
    String query = str.toLowerCase().trim();
    ViewQuery q = createQuery("findByEmail").includeDocs(true).key(query);
    return CollectionUtils.collect(db.queryView(q, Attendant.class), new Transformer() {     
      @Override
      public Object transform(Object arg0) {
        return ((Attendant)arg0).getTicket();
      }
    });
  }
  */
  
  //private List<Attendant> attendees;

  public boolean createAttendant(final Attendant attendant) {
    while (true) {
      long val = new Double((Math.random() * 1000000000000L)+1000000000000L).longValue();
      String str = new Long(val).toString();
      String did = str.substring(str.length()-4)+"-"+
          str.substring(str.length()-8, str.length()-4)+"-"+
          str.substring(str.length()-12, str.length()-8);
      attendant.getTicket().setId(val);
      attendant.getTicket().setDisplayIdentifier(did);
      attendant.setId(did);
      try {
        db.create(attendant);
        play.Logger.info("createAttendant:"+did);
        return true;
      } catch (UpdateConflictException e) {
        try {
          Thread.sleep(200);
        } catch (InterruptedException e1) {
          continue; //ignore it
        }        
      }
    }
  }

  /*
function(doc) {
  var ticket = doc.ticket;
  var toEmit = {};
  if (ticket.email) {
    var parts = ticket.email.toLowerCase().split('@');
    for(var i = 0; i < parts.length; ++i) {
      toEmit[parts[i]] = true;
    }
  }
  var keys = ['lastName', 'firstName'];
  var reSpace = new RegExp('\s+');
  for(var i = 0; i < keys.length; ++i) {
    var parts = ticket[keys[i]].toLowerCase().split(reSpace);
    for(var j = 0; j < parts.length; ++j) {
      toEmit[parts[j]] = true;
    }
  }
  if (ticket.userData) {
    for(var i = 0; i < ticket.userData.length; ++i) {
      if (ticket.userData[i].title == "Company") {
        toEmit[ticket.userData[i].value.toLowerCase()] = true;
        break;
      }
    }
  }
  var toSort = [];
  for(var i in toEmit) {
    toSort.push(i);
  }
  var sorted = toSort.sort().reverse();
  var last = "";
  for(var i = 0; i < sorted.length; ++i) {
    var current = sorted[i];
    if (last.substr(0,current.length) != current) {
      emit(current, null);
    }
    last = current;
  }
  emit(ticket.id, null);
  emit(ticket.displayIdentifier.split('-').join(''), null);
}   */
  @View(name = "freeSearch", map = "function(doc) { var ticket = doc.ticket; var toEmit = {}; if (ticket.email) { var parts = ticket.email.toLowerCase().split('@'); for(var i = 0; i < parts.length; ++i) { toEmit[parts[i]] = true; } } var keys = ['lastName', 'firstName']; var reSpace = /\\\\s+/; for(var i = 0; i < keys.length; ++i) { var parts = ticket[keys[i]].toLowerCase().split(reSpace); for(var j = 0; j < parts.length; ++j) { toEmit[parts[j]] = true; } } if (ticket.userData) { for(var i = 0; i < ticket.userData.length; ++i) { if (ticket.userData[i].title == 'Company') { toEmit[ticket.userData[i].value.toLowerCase()] = true; break; } } } var toSort = []; for(var i in toEmit) { toSort.push(i); } var sorted = toSort.sort().reverse(); var last = ''; for(var i = 0; i < sorted.length; ++i) { var current = sorted[i]; if (last.substr(0,current.length) != current) { emit(current, null); } last = current; } emit(ticket.id, null); emit(ticket.displayIdentifier.split('-').join(''), null); }")
  public Collection<Attendant.Ticket> find(String str) {
    String query = str.toLowerCase().trim();
    String[] minus = query.split("-");
    if (minus.length > 1) {
      StringBuilder sb = new StringBuilder();
      for(String i : minus) {
        sb.append(i);
      }
      query = sb.toString();
    }
    String end = query + "z";
    final Set<String> set = new HashSet<String>();
    ViewQuery q = createQuery("freeSearch").includeDocs(true).limit(50).startKey(query).endKey(end);
    return CollectionUtils.collect(CollectionUtils.select(db.queryView(q, Attendant.class), new Predicate() {
      
      @Override
      public boolean evaluate(Object arg0) {
        String id = ((Attendant)arg0).getId();
        if (set.contains(id)) {
          return false;
        }
        set.add(id);
        return true;
      }
    }), new Transformer() {     
      @Override
      public Object transform(Object arg0) {
        return ((Attendant)arg0).getTicket();
      }
    });
  }
/*
    if (attendees == null) {
      //init();
      return null;
    }
    final String[] search = str.toLowerCase().replace("-", "").split("\\s+");
    return CollectionUtils.collect(
        CollectionUtils.select(attendees, new Predicate() {

          @Override
          public boolean evaluate(Object arg0) {
            final Attendant attendant = ((Attendant) arg0);
            final Attendant.Ticket ticket = attendant.getTicket();
            for (String s : search) {
              if (ticket.getEmail() != null) {
                for (String n : ticket.getEmail().toLowerCase().split("@")) {
                  if (n.startsWith(s)) {
                    return true;
                  }
                }
              }
              for (String n : ticket.getLastName().toLowerCase().split("\\s+")) {
                if (n.startsWith(s)) {
                  return true;
                }
              }
              for (String n : ticket.getFirstName().toLowerCase().split("\\s+")) {
                if (n.startsWith(s)) {
                  return true;
                }
              }
              if (Long.toString(ticket.getId()).startsWith(s)) {
                return true;
              }
              if (ticket.getDisplayIdentifier().replace("-", "").startsWith(s)) {
                return true;
              }
            }
            return false;
          }
        }), new Transformer() {

          @Override
          public Object transform(Object arg0) {
            // TODO Auto-generated method stub
            return ((Attendant) arg0).getTicket();
          }
        });
  private void init() {
    ObjectMapper om = new ObjectMapper();
    attendees = new ArrayList<Attendant>();
    for (File f : new File(path).listFiles()) {
      if (f.getName().endsWith(".ticket.json")) {
        try {
          attendees.add(om.readValue(f, Attendant.class));
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    System.err.println("COUNT:" + attendees.size());
  }
*/
}

//package services;
//
//import java.util.List;
//import java.util.Observable;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//import org.ektorp.changes.ChangesCommand;
//import org.ektorp.changes.ChangesFeed;
//import org.ektorp.changes.DocumentChange;
//import org.ektorp.impl.StdCouchDbConnector;
//
//public class Attendants extends Observable implements Runnable {
//private class Fetchers implements Runnable {
//  public void run() {
//
//    final BlockingQueue<DocumentChange> q = new LinkedBlockingQueue<DocumentChange>();
//    final ChangesCommand cmd = new ChangesCommand.Builder().since(0).build();
//    final StdCouchDbConnector db = new StdCouchDbConnector("attendants",
//        CouchDB.connection());
//    final List<DocumentChange> feed = db.changes(cmd);
//
//    q.addAll(feed);
//    /*
//     * LOGGER.info("Initial Read until len: {}:{}", feed, feed
//     * .get(feed.size() - 1).getSequence());
//     */
//    final Streamies self = this;
//    final Completed c = new Completed(feed.get(feed.size() - 1).getSequence()) {
//
//      @Override
//      public void completed(long last) {
//        LOGGER.info("Initial Read completed until: {}", last);
//        final ChangesCommand cmd = new ChangesCommand.Builder().since(last)
//            .build();
//        final ChangesFeed feed = db.changesFeed(cmd);
//        self.setChanged();
//        self.notifyObservers(Streamies.INITIALIZE);
//        while (feed.isAlive()) {
//          try {
//            final DocumentChange change = feed.next();
//            q.add(change);
//          } catch (InterruptedException e) {
//            e.printStackTrace();
//          }
//        }
//
//      }
//    };
//    new Thread(new Fetchers(q, c)).start();
//    new Thread(new Fetchers(q, c)).start();
//  }
//}
//
//private Attendants my = null;
//public Attendants init() {
//  new Thread(my).start();
//  return my;
//}
//}
//*/

