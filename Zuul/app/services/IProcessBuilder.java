package services;

import java.io.IOException;

public interface IProcessBuilder {
  IProcessBuilder command(String[] args);
  IProcess start() throws IOException; 
}
