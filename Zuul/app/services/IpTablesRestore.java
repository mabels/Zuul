package services;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;

import play.Logger;
import play.Play;
import services.IpTables.Command;

public class IpTablesRestore {

  private boolean started = false;

 

  public IpTablesRestore(BlockingQueue<IpTables.Command> cmdQueue) {
    this.cmdQueue = cmdQueue;
  }
  private final BlockingQueue<IpTables.Command> cmdQueue;

  private IProcessBuilder processBuilder = new TestableProcessBuilder();

  public void injectProcessBuilder(IProcessBuilder inject) {
    processBuilder = inject;
  }
  
  public interface Action {
    void done(Exception e);
  }

  public void start(Action action) {
    synchronized (this) {
      if (started) {
        return;
      }
      final String iptables = Play.configuration.getProperty("cmd");
      processBuilder.command(new String[] { "/bin/sh", "-c", iptables + "--noflush" });
      new Thread(new Runnable() {
        public void run() {
          try {
            final IProcess process = processBuilder.start();
            final List<Thread> children = new LinkedList<Thread>();
            children.add(new Thread(new InStream("stdout", process
                .getInputStream())));
            children.add(new Thread(new InStream("stderr", process
                .getErrorStream())));
            children.add(new Thread(new Runnable() {
              public void run() {
                String lastTable = null;
                final OutputStream out = process.getOutputStream();
                while (true) {
                  try {
                    final IpTables.Command tac = cmdQueue.take();
                    final StringBuffer sb = new StringBuffer();
                    if (tac.table != null
                        && (lastTable == null || !lastTable.equals(tac.table))) {
                      sb.append("*");
                      sb.append(tac.table);
                      sb.append("\n");
                    }
                    sb.append(tac.getLine());
                    sb.append("\n");
                    out.write(sb.toString().getBytes("utf-8"));
                    out.flush();
                    lastTable = tac.table;
                  } catch (Exception e) {
                    Logger.error(e, "cmdQueue Processor aborted");
                    return;
                  } finally {
                    IOUtils.closeQuietly(out);
                  }
                }
              }
            }));
            Logger.info("iptables:starting:threads");
            for (Thread t : children) {
              t.start();
            }
            started = true;
            Logger.info("iptables:completed:{}", process.waitFor());
            for (Thread t : children) {
              t.interrupt();
            }
          } catch (Exception e) {
            Logger.error(e, "Creation of cmd[{}] failed", iptables);
          }
        }
      }).start();
    }
  }

}