package ognl.test.race;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RaceTest {

    @Test
    void testOgnlRace() {
        int concurrent = 128;
        final int batchCount = 2000;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch wait = new CountDownLatch(concurrent);
        final AtomicInteger errCount = new AtomicInteger(0);

        final Person person = new Person();
        for (int i = 0; i < concurrent; i++) {
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    // ignore
                }
                for (int j = 0; j < batchCount; j++) {
                    if (j % 2 == 0) {
                        runValue(person, "yn", errCount);
                    } else {
                        runValue(person, "name", errCount);
                    }
                }
                wait.countDown();
            });
            thread.setName("work-" + i);
            thread.start();
        }
        start.countDown();
        try {
            wait.await();
        } catch (InterruptedException e) {
            // ignore
        }
        assertEquals(0, errCount.get());
    }


    private void runValue(Person person, String name, AtomicInteger errCount) {
        OgnlContext context = Ognl.createDefaultContext(person);
        try {
            Ognl.getValue(name, context);
        } catch (OgnlException e) {
            errCount.incrementAndGet();
            // ignore
        }
    }

}
