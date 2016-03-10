package org.ognl.test.objects;

import java.util.Arrays;
import java.util.List;

public class MethodTestMethods {
    //---------------------------------------------------------------------
    // TestCase for https://github.com/jkuhnert/ognl/issues/17 -  ArrayIndexOutOfBoundsException when trying to access BeanFactory
    // Implementation of BeanFactory interface
    //---------------------------------------------------------------------

    public Object getBean(String name) {
        return "NamedBean: "+name;
    }

    public <T> T getBean(String name, Class<T> requiredType) {
        return (T) ("NamedTypedBean: "+name+" "+requiredType.getSimpleName());
    }

    public <T> T getBean(Class<T> requiredType) {
        return (T) ("TypedBean: "+requiredType.getSimpleName());
    }

    public Object getBean(String name, Object... args) {
        return "NamedBeanWithArgs: "+name+" "+Arrays.toString(args);
    }

    //---------------------------------------------------------------------
    // https://issues.apache.org/jira/browse/OGNL-250 -  OnglRuntime getMethodValue fails to find method matching propertyName
    //---------------------------------------------------------------------

    private String testProperty = "Hello World!";
    
    public String testProperty() {
        return testProperty;
    }

    //---------------------------------------------------------------------
    // Tests related to https://github.com/jkuhnert/ognl/issues/16
    // Argument matching tests
    //---------------------------------------------------------------------

    public String argsTest1(Object[] data) {
        return "Array: "+Arrays.toString(data);
    }

    public String argsTest2(List<Object> data) {
        return "List: "+data;
    }

    public String argsTest3(Object[] data) {
        return "Array: "+Arrays.toString(data);
    }

    public String argsTest3(List<Object> data) {
        return "List: "+data;
    }

}
