package helpers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/*
IP address       HW type     Flags       HW address            Mask     Device
192.168.176.112  0x1         0x2         c8:bc:c8:4f:d4:66     *        br0
192.168.176.190  0x1         0x2         54:04:a6:cc:b0:68     *        br0
 */
public class ResolvArp {
  public static String ip2mac(String ip)  {
    if (ip.equals("192.168.176.113")) {
      return "de:ad:be:ef:ca:fe";
    }
    DataInputStream in = null;
    String found = null;
    try{
      // Open the file that is the first 
      // command line parameter
      FileInputStream fstream = new FileInputStream("/proc/net/arp");
      // Get the object of DataInputStream
      in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String line;
      //Read File Line By Line
      while ((line = br.readLine()) != null) {
        String[] lines = line.split("\\s+");
        if (lines[0].equals(ip)) {
          found = lines[3];
          break;
        }
      }
      in.close();
    }catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      return null;
    } 
    return found;
  }
}
