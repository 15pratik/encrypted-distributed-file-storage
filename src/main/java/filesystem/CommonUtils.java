package filesystem;

import static java.lang.System.getProperty;

public class CommonUtils {
  public static String getRequiredProperty(String property) {
    String prop = getProperty(property);
    if (prop == null) {
      throw new IllegalArgumentException("Missing property " + property);
    }
    return prop;
  }
}
