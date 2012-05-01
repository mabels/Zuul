

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;


 
public class TestAdmin extends FunctionalTest {
     
    @Test
    public void testSearch() {
      Response response = GET("/WiFi/Admin/search");
      assertStatus(200, response);
     
      Document doc = Jsoup.parse(FunctionalTest.getContent(response));
      assertEquals(1, doc.select("input[name=q]").size());
      assertEquals(1, doc.select("table tbody").size());
    }
 
    @Test
    public void testService() {
      Response response = POST("/WiFi/Admin/search/service", "application/x-www-form-urlencoded", "q=sinner");
      assertStatus(200, response);
      StringBuilder sb = new StringBuilder();
      sb.append("<html><body><table>");
      sb.append(FunctionalTest.getContent(response));
      sb.append("</table></body></html>");
      //play.Logger.info(sb.toString());
      Document doc = Jsoup.parse(sb.toString());
      Elements elms = doc.select("tr");
      assertEquals(15, elms.size());
      //play.Logger.info(elms.first().attr("data-displayidentifier").length());
      assertEquals(14, elms.first().attr("data-displayidentifier").length());
    }
    
    
}