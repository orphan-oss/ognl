package ognl.test;

import ognl.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class Issue472DebugTest {

    @Test
    void debugTest() throws OgnlException {
        OgnlRuntime.setMethodAccessor(List.class, new ObjectMethodAccessor() {
            @Override
            public Object callMethod(OgnlContext context, Object target, String methodName, Object[] args) throws MethodFailedException {
                List<?> list = (List<?>) target;
                if (methodName.equals("exists")) {
                    System.out.println("=== In exists() method ===");
                    System.out.println("Context root: " + context.getRoot());
                    System.out.println("Current evaluation: " + context.getCurrentEvaluation());

                    return list.stream()
                            .anyMatch(item -> {
                                try {
                                    System.out.println("  Evaluating lambda for item: " + item);
                                    System.out.println("  Context root before getValue: " + context.getRoot());
                                    System.out.println("  Item (source): " + item);

                                    Object value = Ognl.getValue(args[0], context, item);

                                    System.out.println("  Context root after getValue: " + context.getRoot());
                                    System.out.println("  Result: " + value);

                                    if (!(value instanceof Boolean)) {
                                        throw new RuntimeException("Lambda did not return boolean");
                                    }
                                    return (Boolean) value;
                                } catch (OgnlException e) {
                                    System.out.println("  Exception: " + e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            });
                }
                return super.callMethod(context, target, methodName, args);
            }
        });

        OgnlContext defaultContext = Ognl.createDefaultContext(Map.of(), null, null);
        System.out.println("Initial context root: " + defaultContext.getRoot());

        defaultContext.setRoot(Map.of("test", "value"));
        System.out.println("After setRoot: " + defaultContext.getRoot());

        Object value = Ognl.getValue(
                "myList.exists(:[ #this.equals(test) ])",
                defaultContext,
                Map.of("myList", List.of("value"))
        );

        System.out.println("Final result: " + value);
    }
}
