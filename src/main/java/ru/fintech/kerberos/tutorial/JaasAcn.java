package ru.fintech.kerberos.tutorial;

import com.sun.security.auth.callback.TextCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fintech.kerberos.util.SystemPropertiesUtil;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * This JaasAcn-override application attempts to authenticate a user
 * and reports whether or not the authentication was successful.
 * <p>
 * To execute code:
 * mkdir compiled
 * javac src/main/java/ru/fintech/kerberos/tutorial/JaasAcn-override.java -d compiled
 * <p>
 * Execute the JaasAcn-override application, specifying<br>
 * java -cp compiled -Djava.security.krb5.realm=HOPTO.ORG
 *      -Djava.security.krb5.kdc=kerbserver.hopto.org
 *      -Djava.security.auth.login.config=src/main/resources/tutorial/jaas.conf
 *      ru.fintech.kerberos.tutorial.JaasAcn-override
 * <p>
 * To run an application with a security manager, simply invoke the
 * interpreter with a -Djava.security.manager argument included on the command line.
 * <p>
 * If you try invoking JaasAcn-override with a security manager but without
 * specifying any policy file, you will get the following:
 * WARNING: A command line option has enabled the Security Manager
 * WARNING: The Security Manager is deprecated and will be removed in a future release
 * Cannot create LoginContext. access denied ("javax.security.auth
 * .AuthPermission" "createLoginContext.JaasSample")<br>
 * To execute code with a Security Manager:<br>
 * 1) mkdir compiled<br>
 * 2) javac src/main/java/ru/fintech/kerberos/tutorial/JaasAcn-override.java -d compiled<br>
 * 3) jar -cvf compiled/JaasAcn-override.jar compiled/ru/fintech/kerberos/tutorial/JaasAcn-override.class<br>
 * 4) Create a policy file granting the code in the JAR file the
 * required permission.<br>The permission that is needed by code
 * attempting to instantiate a LoginContext is a
 * javax.security.auth.AuthPermission with target "createLoginContext.<entry name>".
 * Here, <entry name> refers to the name of the login configuration file entry
 * that the application references in its instantiation of LoginContext.
 *
 * Thus, the permission that needs to be granted to JaasAcn-override.jar is
 * permission javax.security.auth.AuthPermission "createLoginContext.JaasSample";
 */
public class JaasAcn {
  private final static Logger log = LoggerFactory.getLogger(JaasAcn.class);

  // Obtain a LoginContext, needed for authentication.
  // Tell it to use the LoginModule implementation specified by the
  // entry named "JaasSample" in the JAAS login configuration
  // file and to also use the specified CallbackHandler.
  public static void main(String[] args) {
    System.out.println("Security Manager:" + System.getSecurityManager());

    log.info("java.security.manager: {}",
        System.getProperty("java.security.manager"));
    SystemPropertiesUtil.loadSystemProperties("system.properties");
    log.info("java.security.auth.login.config: {}",
        System.getProperty("java.security.auth.login.config"));
    log.info("java.security.manager: {}",
        System.getProperty("java.security.manager"));
    log.info("java.security.policy: {}",
        System.getProperty("java.security.policy"));

    // 1) Instantiate a LoginContext.
    LoginContext lc = null;
    try {
      // JaasSample          - The name of an entry in the JAAS login configuration file
      // TextCallbackHandler - A CallbackHandler instance

      // When a LoginModule needs to communicate with the user,
      // for example to ask for a user name and password, it does not do so directly
      // That is because there are various ways of communicating with a user,
      // and it is desirable for LoginModules to remain independent
      // of the different types of user interaction.
      // Rather, the LoginModule invokes a CallbackHandler
      // to perform the user interaction and obtain the requested information,
      // such as the user name and password.
      // (CallbackHandler is an interface in the javax.security.auth.callback pkg.)

      // The LoginContext forwards that instance to the underlying LoginModule
      // (in our case Krb5LoginModule).
      // An application typically provides its own CallbackHandler implementation.
      // A simple CallbackHandler, TextCallbackHandler,
      // is provided in the com.sun.security.auth.callback package
      // to output information to and read input from the command line.
      lc = new LoginContext("JaasSample", new TextCallbackHandler());
    } catch (LoginException | SecurityException ex) {
      log.error("Cannot create LoginContext. {}", ex.getMessage());
      System.exit(-1);
    }

    // 2) Call the LoginContext's login method.
    // attempt authentication
    try {
      // The LoginContext instantiates a new empty javax.security.auth.Subject object
      // (which represents the **user** or **service** being  authenticated).
      // The LoginContext constructs the configured LoginModule
      // (in our case Krb5LoginModule) and initializes it with this new Subject and TextCallbackHandler.

      // The LoginContext's login method then calls methods in the Krb5LoginModule
      // to perform the login and authentication.

      // The Krb5LoginModule will utilize the TextCallbackHandler to obtain the user name and password.

      // Then the Krb5LoginModule will use this information
      // to get the user credentials from the Kerberos KDC.

      // If authentication is successful, the Krb5LoginModule populates the Subject with a
      // (1) Kerberos **Principal** representing the user
      // (2) the user's credentials (TGT).

      // The calling application can subsequently **retrieve** the authenticated Subject
      // by calling the LoginContext's **getSubject** method,
      // although doing so is not necessary for this tutorial.
      lc.login();
    } catch (LoginException ex) {
      log.error("Authentication failed:");
      log.error(" {}", ex.getMessage());
      System.exit(-1);
    }

    log.info("Authentication succeeded!");
  }
}
