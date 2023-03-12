package ru.fintech.kerberos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class SystemPropertiesUtil {
  private final static Logger log = LoggerFactory.getLogger(SystemPropertiesUtil.class);

  private SystemPropertiesUtil() {
    throw new UnsupportedOperationException(
        "This is a utility class and cannot be instantiated");
  }

  public static void loadSystemProperties(String propertiesFile) {
    try (InputStream inputStream = PropertiesUtil.class.getClassLoader()
        .getResourceAsStream(propertiesFile))
    {
      Properties systemProperties = new Properties(System.getProperties());
      systemProperties.load(inputStream);
      systemProperties.keySet().removeAll(System.getProperties().keySet()); // arguments override settings in system.properties file
      System.setProperties(systemProperties);  // changes the set of system properties for the current running application
//      System.getProperties().list(System.out);  // for debugging
    } catch (IOException e) {
      log.error("Error while trying loading system properties from " +
          "system.properties file:\n");
      throw new RuntimeException(e);
    }
  }
}
