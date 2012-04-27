package controllers;

import helpers.SpringUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import play.mvc.Controller;
import play.mvc.Http.Response;
import services.Attendee;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class WiFi extends Controller {

  public static void qrCode() throws IOException, WriterException {
    Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode("http://www.google.com",
        BarcodeFormat.QR_CODE, 300, 300, hintMap);
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
    renderBinary(bais);
  }

  public static void catchAll() {
    render();
  }

  public static void displayId() {
    final Collection<Attendee.Attendant.Ticket> result = SpringUtils
        .getInstance().getBean(Attendee.class).find(params.get("displayId"));
    if (result.size() > 0) {
      Attendee.Attendant.Ticket ticket = result.iterator().next();
      render(ticket);
    } else {
      render("WiFi/displayIdNotFound.html");
    }
  }

  public static class Login {
    public String host;
    public String remoteAddress;
    public String url;
    public String secure;
    public String code;
    public Boolean granted = false;
  }

  public static void askLogin(Login login) {
    System.err.println("WIFI:login");
    if (login.code != null &&

    login.code.equals("meno")) {
      login.granted = true;
      render("WiFi/loggedIn.html", login);
      return;
    }
    render(login);
  }

}
