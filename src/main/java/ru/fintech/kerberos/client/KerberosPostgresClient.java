package ru.fintech.kerberos.client;

import com.sun.security.auth.module.Krb5LoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fintech.kerberos.util.SystemPropertiesUtil;

import javax.security.auth.Subject;
import javax.security.auth.spi.LoginModule;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.sql.DriverManager.getConnection;

public class KerberosPostgresClient {
  private final static Logger log = LoggerFactory.getLogger(KerberosPostgresClient.class);

  static {
//    System.setProperty("java.security.krb5.conf", "c:/tmp/krb5.ini");
//    System.setProperty("java.security.krb5.realm", "<domain>");
//    System.setProperty("java.security.krb5.kdc", "<domain>");
//    System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
//    System.setProperty("java.security.auth.login.config", "c:/tmp/jaas.conf");
  }

  public static void main(String[] args) throws Exception {
    //    SystemPropertiesUtil.loadSystemProperties("system.properties");
    //
    //    try (Connection conn = ConnectionPool.get()) {
    //      Statement statement = conn.createStatement();
    //      log.info("Object statement {} was successfully created", statement);
    //      System.getProperties().list(System.out);
    //    } catch (Exception e) {
    //      e.printStackTrace();
    //    }
    test2();
  }

  public static void test() throws Exception {
    SystemPropertiesUtil.loadSystemProperties("system.properties");
    System.getProperties().list(System.out);
    String url = "jdbc:postgresql://159.223.215.223:5432/postgres";
    Properties properties = new Properties();
    properties.setProperty("JAASConfigName", "pgjdbc");
    try (Connection conn = getConnection(url, properties)) {
      conn.createStatement();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void test2() throws Exception {
    LoginModule krb5Module = new Krb5LoginModule();
    Subject serviceSubject = new Subject();
    Map<String, String> options = new HashMap<>();
    options.put("principal", "client1@HOPTO.ORG");
    options.put("useKeyTab", "true");
    options.put("doNotPrompt", "true");
    options.put("keyTab", "target/classes/client.keytab");
    options.put("isInitiator", "true");
    options.put("refreshKrb5Config", "true");
    krb5Module.initialize(serviceSubject, null, null, options);
    krb5Module.login();
    krb5Module.commit();
    String url = "jdbc:postgresql://159.223.215.223:5432/postgres";

    try (
        Connection connection = Subject.doAs(serviceSubject,
            (PrivilegedExceptionAction<Connection>) () -> getConnection(url))
    ) {}
  }
}
