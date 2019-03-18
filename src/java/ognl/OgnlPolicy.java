package ognl;

import java.lang.reflect.ReflectPermission;
import java.security.*;
import java.util.Enumeration;

/**
 * @author Yasser Zamani
 * @since 3.1.23
 */
class OgnlPolicy extends Policy {
    private Policy _parentPolicy;
    private Permissions _permissions;

    OgnlPolicy(Policy parentPolicy, Permissions permissions) {
        _parentPolicy = parentPolicy;

        _permissions = new Permissions();
        _permissions.add(new SecurityPermission("setPolicy"));
        _permissions.add(new RuntimePermission("setSecurityManager"));
        _permissions.add(new ReflectPermission("suppressAccessChecks"));
        _permissions.add(new RuntimePermission("getProtectionDomain"));

        if (permissions != null) {
            Enumeration<Permission> furtherPermissions = permissions.elements();
            while (furtherPermissions.hasMoreElements()) {
                _permissions.add(furtherPermissions.nextElement());
            }
        }
    }

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        Permissions result = new Permissions();
        if (_parentPolicy != null) {
            Enumeration<Permission> parentPermissions = _parentPolicy.getPermissions(domain).elements();
            while (parentPermissions.hasMoreElements()) {
                result.add(parentPermissions.nextElement());
            }
        }
        Enumeration<Permission> ognlPermissions = _permissions.elements();
        while (ognlPermissions.hasMoreElements()) {
            result.add(ognlPermissions.nextElement());
        }
        return result;
    }

    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {
        if (_permissions.implies(permission)) {
            return true;
        }
        if (_parentPolicy != null) {
            return _parentPolicy.implies(domain, permission);
        }
        return true;
    }
}
