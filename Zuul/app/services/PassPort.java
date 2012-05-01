package services;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ektorp.support.CouchDbDocument;
import org.ektorp.support.TypeDiscriminator;

@TypeDiscriminator("doc._id")
public class PassPort extends CouchDbDocument {
  private static final long serialVersionUID = 1L;

  // @TypeDiscriminator
  private String passPortId;
  private String displayId;
  private boolean sendEmail = true;

  public static class Ip2Mac implements Serializable {

    private static void iptables(String[] opts) {
      iptables(Arrays.asList(opts));
    }

    private static void iptables(Collection<String> opts) {
      List<String> cmd = new ArrayList<String>();
      cmd.add("/usr/bin/sudo");
      cmd.add("/sbin/iptables");
      cmd.addAll(opts);

      String str = StringUtils.join(cmd, " ");
      play.Logger.info("Starting:" + str + ":"
          + play.Play.configuration.get("application.mode"));
      if (play.Play.configuration.get("application.mode").equals("prod")) {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        try {
          pb.start();
        } catch (IOException e) {
          play.Logger.error("Failed to start:IOException:" + str);
        }
      }
    }

    public static void clearIPTables() {
      iptables(new String[] { "-t", "mangle", "-F", "FREE_MACS" });
      iptables(new String[] { "-t", "mangle", "-A", "FREE_MACS", "-j", "RETURN" });
    }

    private static final long serialVersionUID = 1L;
    private String ip;
    private String mac;

    public String getUpdated() {
      return updated;
    }

    public void setUpdated(String updated) {
      this.updated = updated;
    }

    public long getPid() {
      return pid;
    }

    public void setPid(long pid) {
      this.pid = pid;
    }

    private String updated;
    private long pid;

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
    if (clients == null) {
      return new ArrayList<Ip2Mac>();
    } else {
      return clients;
    }
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

  private static String getCurrentTimeStamp() {
    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// dd/MM/yyyy
    Date now = new Date();
    String strDate = sdfDate.format(now);
    return strDate;
  }

  private static long transactionId = 0;

  public static long nextTransactionId() {
    return ++transactionId;
  }

  private static long getProcessID() {
    if (transactionId == 0) {
      RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();

      String jvmName = bean.getName();
      transactionId = Long.valueOf(jvmName.split("@")[0]);
    }
    return transactionId;
  }

  public boolean openFireWall() {
    List<Ip2Mac> clients = this.getClients();
    if (clients == null) {
      return false;
    }
    String now = PassPort.getCurrentTimeStamp();
    Long pid = PassPort.getProcessID();
    boolean changed = false;
    for (Ip2Mac client : clients) {
      if (client.getPid() == pid) {
        continue;
      }
      play.Logger.info("open fw for: ip(" + client.getIp() + ")mac("
          + client.getMac() + ")");
      for (String deladd : new String[] { "-D", "-I" }) {
        PassPort.Ip2Mac.iptables(new String[] { deladd, "FREE_MACS", "-t",
            "mangle", "-p", "all", "-m", "mac", "--mac-source",
            client.getMac(), "-s", client.getIp(), "-j", "MARK", "--set-mark",
            "0x1205" });
      }
      changed = true;
      client.setPid(pid);
      client.setUpdated(now);
    }
    return changed;
  }

}
