package controllers;

import helpers.ResolvArp;
import helpers.SpringUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import play.mvc.Controller;
import play.mvc.Http.Response;
import services.Attendant;
import services.Attendants;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class WiFi extends Controller {

  public static ByteArrayInputStream makeQrCode(String passPortId, int size) throws WriterException, IOException {
    Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode("https://wifi.nextconf.eu/"
        + passPortId, BarcodeFormat.QR_CODE, size, size, hintMap);
    // Make the BufferedImage that are to hold the QRCode
    int matrixWidth = bitMatrix.getWidth();
    BufferedImage image = new BufferedImage(matrixWidth, matrixWidth,
        BufferedImage.TYPE_INT_RGB);
    image.createGraphics();
    Graphics2D graphics = (Graphics2D) image.getGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, matrixWidth, matrixWidth);

    // Paint and save the image using the ByteMatrix
    graphics.setColor(Color.BLACK);
    for (int i = 0; i < matrixWidth; i++) {
      for (int j = 0; j < matrixWidth; j++) {
        if (bitMatrix.get(i, j)) {
          graphics.fillRect(i, j, 1, 1);
        }
      }
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    Response.current().contentType = "image/png";
    return bais;
  }

  public static void qrCode(String passPortId) throws WriterException, IOException {
    renderBinary(makeQrCode(passPortId, 200));
  }

  public static void catchAll() {
    render();
  }
  

  public static class Login {
    public String host;
    public String remoteAddress;
    public String url;
    public String secure;
    public String code;
    public String macAddress;
    public Boolean granted = false;
  }

  public static void askLogin(Login login) {
    System.err.println("WIFI:login");
    if (login.macAddress == null) {
      login.macAddress = ResolvArp.ip2mac(login.remoteAddress);
    }
    if (login.code != null) {
      String code = Pass.tryLogin(login.code.trim());
      if (code.equals("granted")) {
        login.granted = true;
        redirect("/"+login.code);
        return;
      }
    }
    render(login);
  }

}
