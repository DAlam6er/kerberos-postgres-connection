package ru.fintech.kerberos.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fintech.kerberos.util.ConnectionPool;

import java.sql.Connection;
import java.sql.Statement;

public class KerberosPostgresClient {
  private final static Logger log = LoggerFactory.getLogger(KerberosPostgresClient.class);

  public static void main(String[] args) throws Exception {

    try (Connection conn = ConnectionPool.get()) {
      Statement statement = conn.createStatement();
      log.info("Object statement {} was successfully created", statement);
      System.getProperties().list(System.out);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
