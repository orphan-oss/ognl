package ognl.test;

import ognl.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue472SimpleTest {

    @Test
    void testSimpleApiWorks() throws OgnlException {
        OgnlRuntime.setMethodAccessor(List.class, new ObjectMethodAccessor() {
            @Override
            public Object callMethod(OgnlContext context, Object target, String methodName, Object[] args) throws MethodFailedException {
                List<?> list = (List<?>) target;
                if (methodName.equals("exists")) {
                    return list.stream()
                            .anyMatch(item -> {
                                try {
                                    Object value = Ognl.getValue(args[0], context, item);
                                    if (!(value instanceof Boolean)) {
                                        throw new RuntimeException("Lambda did not return boolean");
                                    }
                                    return (Boolean) value;
                                } catch (OgnlException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
                return super.callMethod(context, target, methodName, args);
            }
        });

        Map<String, Object> root = Map.of(
                "test", "value",
                "myList", List.of("value")
        );

        Object value = Ognl.getValue("myList.exists(:[ #this.equals(#root.test) ])", root);
        assertEquals(Boolean.TRUE, value);
    }
}
