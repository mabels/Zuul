package controllers;

import helpers.ResolvArp;
import helpers.SpringUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.ektorp.DocumentNotFoundException;

import play.cache.CacheFor;
import play.mvc.Controller;
import play.mvc.Http.Header;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import services.Attendant;
import services.Attendants;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import controllers.Pass.TryLoginReturn;

public class WiFi extends Controller {

  public static ByteArrayInputStream makeQrCode(String passPortId, int size) throws WriterException, IOException {
    Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode("https://wifi.sinnerschrader.it/"
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

  //@CacheFor("1h")
  public static void qrCode(String passPortId) throws WriterException, IOException {
    renderBinary(makeQrCode(passPortId, 200));
  }

  /*
    proxy_set_header  X-Real-Scheme $scheme;
    proxy_set_header  X-Real-IP     $remote_addr;
    proxy_set_header  X-Real-Host   $http_host;
    proxy_set_header  X-Real-Uri    $request_uri;
   */
  public static void catchAll() {
		Login login = new Login(request);
    render("WiFi/catchAll.html", login);
  }
  

  public static class Login {
		public Login(Request request) {
			Header xRealScheme = request.headers.get("x-real-scheme");
			if (xRealScheme != null) {
				this.secure = xRealScheme.value();
			} else {
				this.secure = request.secure ? "https" : "http";
			}
			Header xRealIp = request.headers.get("x-real-ip");
			if (xRealIp != null) {
				this.remoteAddress = xRealIp.value();
			} else {
				this.remoteAddress = request.remoteAddress;
			}
			Header xRealHost = request.headers.get("x-real-host");
			if (xRealHost != null) {
				this.host = xRealHost.value();
			} else {
				this.host = request.headers.get("host").value();
			}
			Header xRealUri = request.headers.get("x-real-uri");
			if (xRealUri != null) {
				this.url = xRealUri.value();
			} else {
				this.url = request.url;
			}
			this.appUrl = play.Play.configuration.get("application.baseUrl")+"/WiFi/askLogin";
    }
    public String errorClass;
    public String errorText;
		public String appUrl;
    public String host;
    public String remoteAddress;
    public String url;
    public String secure;
    public String macAddress;
		private String code;
    public String getCode() {
      if (code != null) {
        play.Logger.info("getCode:"+code);
        return code;
      }
      if (code_grp1 != null &&
          code_grp2 != null &&
          code_grp3 != null) {
        play.Logger.info("getCode:"+code_grp1+":"+code_grp2+":"+code_grp3);
        return code_grp1.trim() + "-" + code_grp2.trim() + "-" + code_grp3.trim();
      }
      return null;
    }
    public String code_grp1;
    public String code_grp2;
    public String code_grp3;
    public Boolean granted = false;
  }

  public static void askLogin(Login login) {
    play.Logger.info("WIFI:login");
    if (login.macAddress == null) {
      login.macAddress = ResolvArp.ip2mac(login.remoteAddress);
    }
    if (login.getCode() != null) {
      TryLoginReturn tlr = Pass.tryLogin(login.getCode().trim());
      play.Logger.info("tryLogin:return="+ tlr.errorText);
      if (tlr.granted) {
        login.granted = true;
        redirect(play.Play.configuration.get("application.baseUrl")+"/grant/"+login.getCode());
      } else {
        login.errorClass = "error";
        login.errorText = tlr.errorText;
      }
    }
    render(login);
  }

}
