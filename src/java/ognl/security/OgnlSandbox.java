package ognl.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;

/**
 * A signature for {@link OgnlSecurityManager#isAccessDenied()}. Also executes user methods with not any permission.
 *
 * @author Yasser Zamani
 * @since 3.1.24
 */
public class OgnlSandbox {
    public static Object executeMethodBody(final Object target, final Method method, final Object[] argsArray)
            throws InvocationTargetException {

        Permissions p = new Permissions(); // not any permission
        ProtectionDomain pd = new ProtectionDomain(null, p);
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    return method.invoke(target, argsArray);
                }
            }, new AccessControlContext(new ProtectionDomain[]{pd}));
        } catch (PrivilegedActionException e) {
            if (e.getException() instanceof InvocationTargetException) {
                throw (InvocationTargetException) e.getException();
            }
            throw new InvocationTargetException(e);
        }
    }
}
