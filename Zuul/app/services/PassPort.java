package services;

import java.io.Serializable;
import java.util.List;

import org.ektorp.support.CouchDbDocument;
import org.ektorp.support.TypeDiscriminator;
import org.hibernate.engine.jdbc.SerializableBlobProxy;

@TypeDiscriminator("doc._id")
public class PassPort extends CouchDbDocument {
    private static final long serialVersionUID = 1L;
    
    //@TypeDiscriminator
    private String passPortId;
    private String displayId;
    private boolean sendEmail = true;
    public static class Ip2Mac implements Serializable {
      private static final long serialVersionUID = 1L;
      private String ip;
      private String mac;
      public String getIp() {
        return ip;
      }
      public void setIp(String ip) {
        this.ip = ip;
      }
      public String getMac() {
        return mac;
      }
      public void setMac(String mac) {
        this.mac = mac;
      }
    }
    private List<Ip2Mac> clients;
     
    public List<Ip2Mac> getClients() {
      return clients;
    }

    public void setClients(List<Ip2Mac> clients) {
      this.clients = clients;
    }

    public String getDisplayId() {
      return displayId;
    }

    public void setDisplayId(String displayId) {
      this.displayId = displayId;
    }

    private Boolean used;

    public Boolean getUsed() {
      return used;
    }

    public void setUsed(Boolean used) {
      this.used = used;
    }

    public String getPassPortId() {
      return passPortId;
    }

    public void setPassPortId(String passPortId) {
      this.passPortId = passPortId;
    }

    public boolean isSendEmail() {
      return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
      this.sendEmail = sendEmail;
    }
    
}
