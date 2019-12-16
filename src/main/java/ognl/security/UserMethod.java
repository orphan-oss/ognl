package ognl.security;

import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;

/**
 * A signature for {@link OgnlSecurityManager#isAccessDenied(java.security.Permission)}. Also executes user methods with not any permission.
 *
 * @author Yasser Zamani
 * @since 3.1.24
 */
public class UserMethod implements PrivilegedExceptionAction<Object> {
    private final Object target;
    private final Method method;
    private final Object[] argsArray;

    public UserMethod(Object target, Method method, Object[] argsArray) {
        this.target = target;
        this.method = method;
        this.argsArray = argsArray;
    }

    public Object run() throws Exception {
        return method.invoke(target, argsArray);
    }
}
