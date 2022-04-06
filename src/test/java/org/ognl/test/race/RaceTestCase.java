package org.ognl.test.race;


import org.ognl.DefaultMemberAccess;
import org.ognl.Ognl;
import org.ognl.OgnlContext;
import org.ognl.OgnlException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;


public class RaceTestCase {

    @Test
    public void testOgnlRace(){
        int concurrent = 128;
        final int batchCount = 2000;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch wait = new CountDownLatch(concurrent);
        final AtomicInteger errCount = new AtomicInteger(0);

        final Persion persion = new Persion();
        for (int i = 0; i < concurrent;i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for(int j = 0; j < batchCount;j++){
                        if(j % 2 == 0) {
                            runValue(persion, "yn", errCount);
                        } else {
                            runValue(persion, "name", errCount);
                        }
                    }
                    wait.countDown();
                }
            });
            thread.setName("work-"+i);
            thread.start();
        }
        start.countDown();
        try {
            wait.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println("error:" + errCount.get());
        Assert.assertTrue(errCount.get() == 0);
    }



    private void runValue(Persion persion,String name,AtomicInteger errCount) {
        OgnlContext context = new OgnlContext(null,null,new DefaultMemberAccess(false));
        context.setRoot(persion);
        try {
            Object value =  Ognl.getValue(name, context, context.getRoot());
//            System.out.println(value);

        } catch (OgnlException e) {
            errCount.incrementAndGet();
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
