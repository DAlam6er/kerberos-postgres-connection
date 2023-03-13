package ru.fintech.kerberos.tutorial.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fintech.kerberos.tutorial.actions.SampleAction;
import ru.fintech.kerberos.tutorial.authentication.JaasAcn;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import java.security.PrivilegedAction;

/**
 * If you try invoking JaasAzn-override with a security manager but without
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
 * <p>
 * Thus, the permission that needs to be granted to jar file is
 * permission javax.security.auth.AuthPermission "createLoginContext.JaasSample";
 */
public class JaasAzn {
  private final static Logger log = LoggerFactory.getLogger(JaasAzn.class);

  public static void main(String[] args) {
    new JaasAzn().authorize();
  }

  private void authorize() {
    JaasAcn jaasAcn = new JaasAcn();
    jaasAcn.authenticate();
    LoginContext loginContext = jaasAcn.getLoginContext();

    // now try to execute the SampleAction as the authenticated Subject
    Subject mySubject = loginContext.getSubject();
    PrivilegedAction<JaasAzn> action = new SampleAction();
    Subject.doAsPrivileged(mySubject, action, null);
  }
}
