package services;

import java.io.InputStream;
import java.io.OutputStream;

public interface IProcess {
  InputStream getInputStream();
  InputStream getErrorStream();
  OutputStream getOutputStream();
  int waitFor() throws InterruptedException;
}
