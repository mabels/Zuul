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
//  private class Fetchers implements Runnable {
//    public void run() {
//
//      final BlockingQueue<DocumentChange> q = new LinkedBlockingQueue<DocumentChange>();
//      final ChangesCommand cmd = new ChangesCommand.Builder().since(0).build();
//      final StdCouchDbConnector db = new StdCouchDbConnector("attendants",
//          CouchDB.connection());
//      final List<DocumentChange> feed = db.changes(cmd);
//
//      q.addAll(feed);
//      /*
//       * LOGGER.info("Initial Read until len: {}:{}", feed, feed
//       * .get(feed.size() - 1).getSequence());
//       */
//      final Streamies self = this;
//      final Completed c = new Completed(feed.get(feed.size() - 1).getSequence()) {
//
//        @Override
//        public void completed(long last) {
//          LOGGER.info("Initial Read completed until: {}", last);
//          final ChangesCommand cmd = new ChangesCommand.Builder().since(last)
//              .build();
//          final ChangesFeed feed = db.changesFeed(cmd);
//          self.setChanged();
//          self.notifyObservers(Streamies.INITIALIZE);
//          while (feed.isAlive()) {
//            try {
//              final DocumentChange change = feed.next();
//              q.add(change);
//            } catch (InterruptedException e) {
//              e.printStackTrace();
//            }
//          }
//
//        }
//      };
//      new Thread(new Fetchers(q, c)).start();
//      new Thread(new Fetchers(q, c)).start();
//    }
//  }
//  
//  private Attendants my = null;
//  public Attendants init() {
//    new Thread(my).start();
//    return my;
//  }
//}
//*/
