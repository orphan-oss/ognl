/**
 *
 */
package ognl.test.objects;

import ognl.ObjectPropertyAccessor;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;
import ognl.enhance.ExpressionCompiler;
import ognl.enhance.UnsupportedCompilationException;

/**
 * Implementation of provider that works with {@link BeanProvider} instances.
 */
public class BeanProviderAccessor extends ObjectPropertyAccessor implements PropertyAccessor {
    public Object getProperty(OgnlContext context, Object target, Object name) throws OgnlException {
        BeanProvider provider = (BeanProvider) target;
        String beanName = (String) name;

        return provider.getBean(beanName);
    }

    /**
     * Returns true if the name matches a bean provided by the provider.
     * Otherwise invokes the super implementation.
     **/
    public boolean hasGetProperty(OgnlContext context, Object target, Object oname) throws OgnlException {
        BeanProvider provider = (BeanProvider) target;
        String beanName = ((String) oname).replaceAll("\"", "");

        return provider.getBean(beanName) != null;
    }

    public String getSourceAccessor(OgnlContext context, Object target, Object name) {
        BeanProvider provider = (BeanProvider) target;
        String beanName = ((String) name).replaceAll("\"", "");

        if (provider.getBean(beanName) != null) {
            context.setCurrentAccessor(BeanProvider.class);
            context.setCurrentType(provider.getBean(beanName).getClass());

            ExpressionCompiler.addCastString(context, "(("
                    + OgnlRuntime.getCompiler().getInterfaceClass(provider.getBean(beanName).getClass()).getName() + ")");

            return ".getBean(\"" + beanName + "\"))";
        }

        return super.getSourceAccessor(context, target, name);
    }

    public String getSourceSetter(OgnlContext context, Object target, Object name) {
        throw new UnsupportedCompilationException("Can't set beans on BeanProvider.");
    }
}
