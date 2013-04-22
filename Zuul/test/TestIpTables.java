import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import play.test.FunctionalTest;
import play.test.UnitTest;
import services.IProcess;
import services.IProcessBuilder;


public class TestIpTables extends UnitTest {
  private static IProcessBuilder processBuilder = new IProcessBuilder() {
    public IProcess start() throws IOException {
      return new IProcess() {
        public int waitFor() throws InterruptedException {
          return 0;
        }
        public OutputStream getOutputStream() {
          return new ByteArrayOutputStream();
        }
        
        @Override
        public InputStream getInputStream() {
          return null; // new ByteArrayInputStream(arg0);
        }
        
        @Override
        public InputStream getErrorStream() {
          // TODO Auto-generated method stub
          return null;
        }
      };
    }
    public IProcessBuilder command(String[] args) {
      return this;
    }
  };
  @Test
  public void testOneCommand() {
    
  }
}
