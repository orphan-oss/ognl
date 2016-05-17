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

    //---------------------------------------------------------------------
    // https://github.com/jkuhnert/ognl/issues/23
    // 'avg' tests
    //---------------------------------------------------------------------
    public double avg(final Iterable<? extends Number> target) {
        double total = 0;
        int size = 0;
        for (final Number element : target) {
            total += element.doubleValue();
            size++;
        }
        return total/size;
    }

    public double avg(final Number[] target) {
        double total = 0;
        for (final Number element : target) {
            total += element.doubleValue();
        }
        return total/target.length;
    }

    public double avg(final byte[] target) {
        double total = 0;
        for (final Number element : target) {
            total += element.doubleValue();
        }
        return total/target.length;
    }

    public double avg(final short[] target) {
        double total = 0;
        for (final Number element : target) {
            total += element.doubleValue();
        }
        return total/target.length;
    }

    public double avg(final int[] target) {
        double total = 0;
        for (final Number element : target) {
            total += element.doubleValue();
        }
        return total/target.length;
    }

    public double avg(final long[] target) {
        double total = 0;
        for (final Number element : target) {
            total += element.doubleValue();
        }
        return total/target.length;
    }

    public double avg(final float[] target) {
        double total = 0;
        for (final Number element : target) {
            total += element.doubleValue();
        }
        return total/target.length;
    }

    public double avg(final double[] target) {
        double total = 0;
        for (final Number element : target) {
            total += element.doubleValue();
        }
        return total/target.length;
    }

    public String[] getStringArray() {
        return new String[] { "Hello", "World" };
    }

    public List<String> getStringList() {
        return Arrays.asList("Hello", "World");
    }

    public List<Object> getObjectList() {
        return Arrays.asList((Object)"Object");
    }

    public String showList(String[] args) {
        return "Strings: " + Arrays.toString(args);
    }

    public String showList(Object[] args) {
        return "Objects: " + Arrays.toString(args);
    }

    public String showStringList(String[] args) {
        return "Strings: " + Arrays.toString(args);
    }
}
