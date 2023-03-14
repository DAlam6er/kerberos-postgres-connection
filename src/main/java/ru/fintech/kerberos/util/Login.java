package ru.fintech.kerberos.util;

import com.sun.security.auth.callback.TextCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class Login {
  private final static int NUMBER_OF_ATTEMPTS = 3;

  private final static Logger log = LoggerFactory.getLogger(Login.class);

  private LoginContext loginContext = null;
  private final String[] arguments;

  /**
   * <p> Instantiate a <code>LoginContext</code> using the
   * provided application classname as the index for the login
   * <code>Configuration</code>. Authenticate the <code>Subject</code>
   * (three retries are allowed).
   * <p>
   *
   * @param arguments the arguments for <code>Login</code>.  The first
   *                  argument must be the class name of the application to be
   *                  invoked once authentication has completed, and the
   *                  subsequent arguments are the arguments to be passed
   *                  to that application's public <code>authenticate</code> method.
   */
  public Login(String[] arguments) {
    this.arguments = arguments.clone();
  }

  public void authenticate() {
    if (arguments == null || arguments.length == 0) {
      log.error("Invalid arguments: Did not provide name of application class.");
      System.exit(-1);
    }

    log.info("Loading system properties...");
    SystemPropertiesUtil.loadSystemProperties("system.properties");
    log.info("java.security.auth.login.config: {}",
        System.getProperty("java.security.auth.login.config"));
    log.info("java.security.manager enabled: {}",
        System.getSecurityManager() != null);
    log.info("java.security.policy: {}",
        System.getProperty("java.security.policy"));

    try {
      log.info("Initiating loginContext...");
      loginContext = new LoginContext(arguments[0], new TextCallbackHandler());
      log.info("loginContext successfully initiated");
    } catch (LoginException | SecurityException ex) {
      log.error("Cannot create LoginContext. {}", ex.getMessage());
      System.exit(-1);
    }

    int i;
    for (i = 0; i < NUMBER_OF_ATTEMPTS; i++) {
      try {
        log.info("Attempt to authenticate...");
        loginContext.login();
        log.info("Authentication succeeded!");
        break;
      } catch (AccountExpiredException aee) {
        log.error("Your account has expired.  " +
            "Please notify your administrator.");
        System.exit(-1);
      } catch (CredentialExpiredException cee) {
        log.error("Your credentials have expired.");
        System.exit(-1);
      } catch (FailedLoginException fle) {
        log.error("Authentication Failed");
        try {
          Thread.sleep(3000);
        } catch (Exception ignored) {}
      } catch (LoginException ex) {
        if (i < NUMBER_OF_ATTEMPTS - 1) {
          log.error("Sorry, try again.");
        }
        log.info("{} incorrect password attempt", i + 1);
      } catch (Exception ex) {
        log.error("Unexpected Exception - unable to continue");
        ex.printStackTrace();
        System.exit(-1);
      }
    }
    if (i == 3) {
      log.error("Exceeded number of authentication attempts");
      System.exit(-1);
    }
  }

  public LoginContext getLoginContext() {
    return loginContext;
  }

  public String[] getArguments() {
    return arguments;
  }
}
