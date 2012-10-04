package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import play.Logger;

class InStream implements Runnable {
  private final InputStream in;
  private final String title;
  public InStream(String title, InputStream in) {
    this.in = in;
    this.title = title;
  }

  public void run() {
    final BufferedReader inp = new BufferedReader(new InputStreamReader(in));
    try {
      while (true) {
        String line;
        line = inp.readLine();
        if (line == null) {
          break;
        }
        Logger.warn("iptables:{}:{}", title, line);
      }
    } catch (IOException e) {
      Logger.error(e, "iptables:{} failed", title);
    } finally {
      IOUtils.closeQuietly(inp);
    }
  }
}
