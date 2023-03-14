package ru.fintech.kerberos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fintech.kerberos.util.Authorize;
import ru.fintech.kerberos.util.Login;

import javax.security.auth.login.LoginContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Run with parameters:
 * java
 * -Djava.security.manager
 * -Djava.security.krb5.realm=HOPTO.ORG
 * -Djava.security.krb5.kdc=kerbserver.hopto.org
 * -Djava.security.policy=target/classes/jaasacn.policy
 * -Djava.security.auth.login.config=target/classes/jaas.conf
 * ru.fintech.kerberos.tutorial.actions.AuthorizedConnection
 */
public class Runner {
  private final static Logger log = LoggerFactory.getLogger(Authorize.class);

  public static void main(String[] args) {
    Login login = new Login(args);
    login.authenticate();
    LoginContext loginContext = login.getLoginContext();

    Authorize authorize = new Authorize();
    log.info("Trying to get the Connection...");
    Optional<Object> maybeObject = authorize.authorize(loginContext, args);
    log.info("Connection successfully received...");

    log.info("Trying to establish connection with database...");
    maybeObject.ifPresent(Runner :: testConnection);
  }

  private static void testConnection(Object object) {
    String sql = "SELECT usename FROM pg_catalog.pg_user;";
    if (object instanceof Connection) {
      try (Connection connection = (Connection) object) {
        log.info("Test connection established.");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        System.out.println("Database usernames:");
        while (resultSet.next()) {
          System.out.println("name: " + resultSet.getString("usename"));
        }
      } catch (SQLException exception) {
        log.error("Error while receiving info");
      }
    }
  }
}
