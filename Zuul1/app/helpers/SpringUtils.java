package helpers;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringUtils {

  private static final SpringUtils instance = new SpringUtils();

  private ApplicationContext context;

  public static SpringUtils getInstance() {
    return instance;
  }

  private SpringUtils() {
    context = new ClassPathXmlApplicationContext("context.xml");
  }

  public <T> T getBean(Class<T> type) {
    return context.getBean(type);
  }

}
