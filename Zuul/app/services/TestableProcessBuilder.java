package services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class TestableProcessBuilder implements IProcessBuilder {    
  ProcessBuilder processBuilder = new ProcessBuilder();
  public IProcess start() throws IOException {
    final Process process = processBuilder.start();
    return new IProcess() {
      public int waitFor() throws InterruptedException {
        return process.waitFor();
      }
      public OutputStream getOutputStream() {
        return process.getOutputStream();
      }
      public InputStream getInputStream() {
        return process.getInputStream();
      }
      public InputStream getErrorStream() {
        return process.getErrorStream();
      }
    }; 
        
  }
  public IProcessBuilder command(String[] args) {
    processBuilder.command(args);
    return this;
  }
}
