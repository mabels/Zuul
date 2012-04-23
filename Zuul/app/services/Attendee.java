package services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.codehaus.jackson.map.ObjectMapper;

public class Attendee {
  public static class Attendant {
  
  }
  private String path;
  public void setDbPath(String path) {
    this.path = path;
  }
  private List<Attendant> attendees;
  public Attendant find(String str) {
    if (attendees == null) {
      init();
    }
    CollectionUtils.collect(CollectionUtils.select(attendees, new Predicate() {
      
      @Override
      public boolean evaluate(Object arg0) {
        // TODO Auto-generated method stub
        return false;
      }
    }), new Transformer() {
      
      @Override
      public Object transform(Object arg0) {
        // TODO Auto-generated method stub
        return null;
      }
    });
    
    return null;
  }
  private void init() {
    ObjectMapper om = new ObjectMapper();
    attendees = new ArrayList<Attendant>();
    for(File f : new File(path).listFiles()) {
      if (f.getName().endsWith(".ticket.json")) {
        try {
          attendees.add(om.readValue(f, Attendant.class));
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
}
