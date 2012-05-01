

import play.test.*;
import play.mvc.*;
import play.mvc.Http.*;
import org.junit.*;
 
public class TestCatchAll extends FunctionalTest {
     
    @Test
    public void testCatchAllePage() {
      Response response = GET("/Catch/All");
      assertStatus(200, response);
      
    }
     
}