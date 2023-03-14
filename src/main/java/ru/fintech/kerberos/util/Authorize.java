package ru.fintech.kerberos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.Optional;

public class Authorize {
  private static final String METHOD_NAME_TO_INVOKE = "get";

  private final static Logger log = LoggerFactory.getLogger(Authorize.class);

  public Optional<Object> authorize(LoginContext loginContext, String[] args) {
    // push the subject into the current ACC
    Optional<Object> result = Optional.empty();
    log.debug("Trying to execute some actions as the authenticated Subject");
    //      result = Subject.doAsPrivileged(loginContext.getSubject(), new MyAction(args), null);
    result = Subject.doAsPrivileged(loginContext.getSubject(),
        (PrivilegedAction<Optional<Object>>) () -> {
          log.debug("Getting the ContextClassLoader...");
          ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

          try {
            log.debug("Getting the application class's main method...");
            Class<?> clazz = Class.forName(args[0], true, classLoader);
//            Class<?>[] params = {arguments.getClass()};
//            Method method = clazz.getMethod("get", params);
            Method method = clazz.getMethod(METHOD_NAME_TO_INVOKE);

            String[] appArgs = new String[args.length - 1];
            System.arraycopy(args, 1, appArgs, 0, args.length - 1);
            Object[] args1 = {appArgs};
            log.debug("Invoking method with the remaining args");
            Object result1 = method.invoke(null);
//            Object result1 = method.invoke(null /*ignored*/, null);
            return Optional.ofNullable(result1);
          } catch (ClassNotFoundException | InvocationTargetException |
                   NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        }, null);
    return result;
  }
}
