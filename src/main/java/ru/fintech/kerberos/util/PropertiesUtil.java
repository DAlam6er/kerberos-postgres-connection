package ru.fintech.kerberos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {
  private static final Properties PROPERTIES = new Properties();

  private final static Logger log = LoggerFactory.getLogger(PropertiesUtil.class);

  static {
    loadProperties();
  }

  private PropertiesUtil() {
    throw new UnsupportedOperationException(
        "This is a utility class and cannot be instantiated");
  }

  private static void loadProperties() {
    try (InputStream inputStream = PropertiesUtil.class.getClassLoader()
        .getResourceAsStream("application.properties"))
    {
      PROPERTIES.load(inputStream);
    } catch (IOException e) {
      log.error("Error while trying loading properties from " +
          "application.properties file:\n");
      throw new RuntimeException(e);
    }
  }

  public static String get(String key) {
    return PROPERTIES.getProperty(key);
  }
}
