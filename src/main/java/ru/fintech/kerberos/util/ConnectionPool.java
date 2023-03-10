package ru.fintech.kerberos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionPool {
  private static final String DB_KEY = "db.driver";
  private static final String URL_KEY = "db.url";
  private static final String USER_KEY = "db.user";
  private static final String PASSWORD_KEY = "db.password";
  private static final Integer DEFAULT_POOL_SIZE = 10;
  private static final String POOL_SIZE_KEY = "db.pool.size";

  private final static Logger log = LoggerFactory.getLogger(ConnectionPool.class);


  private static BlockingQueue<Connection> pool;
  private static List<Connection> sourceConnections;

  static {
    loadDriver();
    initConnectionPool();
  }

  private ConnectionPool() {
    throw new UnsupportedOperationException(
        "This is a utility class and cannot be instantiated");
  }


  private static void loadDriver() {
    try {
      Class.forName(PropertiesUtil.get(DB_KEY));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static void initConnectionPool() {
    String poolSize = PropertiesUtil.get(POOL_SIZE_KEY);
    int size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
    pool = new ArrayBlockingQueue<>(size);
    sourceConnections = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      Connection connection = open();

      Connection proxyConnection = (Connection) Proxy.newProxyInstance(
          ConnectionPool.class.getClassLoader(),
          new Class[]{Connection.class},
          (proxy, method, args) -> method.getName().equals("close")
              ? pool.add((Connection) proxy)
              : method.invoke(connection, args)
      );
      pool.add(proxyConnection);
      sourceConnections.add(connection);
    }
  }

  private static Connection open() {
    try {
      return DriverManager.getConnection(
          PropertiesUtil.get(URL_KEY),
          PropertiesUtil.get(USER_KEY),
          PropertiesUtil.get(PASSWORD_KEY));
    } catch (SQLException e) {
      log.error("The connection attempt failed:\n");
      throw new RuntimeException(e);
    }
  }

  public static Connection get() {
    try {
      return pool.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void close() {
    try {
      for (Connection sourceConnection : sourceConnections) {
        sourceConnection.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
