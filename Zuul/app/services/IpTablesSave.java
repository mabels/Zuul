package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.Completion;

import org.apache.commons.io.IOUtils;

import play.Logger;
import play.Play;

public class IpTablesSave {

  private boolean started = false;
  private TestableProcessBuilder processBuilder = new TestableProcessBuilder();

  public interface Action {
    void done();
    void add(String table, String command);
  }

  public void start(final Action action) {
    synchronized (this) {
      if (started) {
        return;
      }
      final String iptables = Play.configuration.getProperty("cmd.save");
      processBuilder.command(new String[] { "/bin/sh -c", iptables });
      new Thread(new Runnable() {
        public void run() {
          try {
            final IProcess process = processBuilder.start();
            final List<Thread> children = new LinkedList<Thread>();
            children.add(new Thread(new Runnable() {
              private final InputStream in = process.getInputStream();

              public void run() {
                final BufferedReader inp = new BufferedReader(
                    new InputStreamReader(in));
                try {
                  String table = null;
                  while (true) {
                    String line = inp.readLine();
                    if (line == null) {
                      break;
                    }
                    line = line.trim();
                    if (line.isEmpty()) {
                      continue;
                    }
                    if (line.startsWith("#") || line.startsWith(":")) {
                      continue;
                    }
                    if (line.startsWith("*")) {
                      table = line.substring("*".length());
                      continue;
                    }
                    if (table == null) {
                      Logger.error("iptable-save no table specified");
                      continue;
                    }
                    action.add(table, line);
                  }
                } catch (IOException e) {
                  Logger.error(e, "iptables-save:{} failed");
                } finally {
                  IOUtils.closeQuietly(inp);
                }
              }
            }));
            children.add(new Thread(new InStream("stderr", process
                .getErrorStream())));
            Logger.info("iptables:starting:threads");
            for (Thread t : children) {
              t.start();
            }
            started = true;
            Logger.info("iptables:completed:{}", process.waitFor());
            action.done();
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
