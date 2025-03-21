package org.jrl.utils.thread.client;

import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.api.JrlThreadShutdownHandler;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadPoolExecutor;

class ThreadShutdownTest {

    @Test
    void testShutdown() {
        JrlThreadPool testShutdown1 = JrlThreadUtil.newPool("testShutdown1", JrlThreadPoolConfig.builder().shutdownOrder(1).build());
        JrlThreadPool testShutdown2 = JrlThreadUtil.newPool("testShutdown2", JrlThreadPoolConfig.builder().shutdownOrder(2).build());
        JrlThreadPool testShutdown3 = JrlThreadUtil.newPool("testShutdown3", JrlThreadPoolConfig.builder().shutdownOrder(3).shutdownFailHandler(() -> new JrlThreadShutdownHandler() {
            @Override
            public void onShutdown(ThreadPoolExecutor executor) {

            }

            @Override
            public void onFail(ThreadPoolExecutor executor) {
                System.out.println("shutdown fail ! " + executor.toString());
            }
        }).build());
        testShutdown1.execute(()-> {
            try {
                Thread.sleep(10000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(1111);
        });
        testShutdown2.execute(()-> {
            try {
                Thread.sleep(10000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(2222);
        });
        testShutdown3.execute(()-> {
            try {
                Thread.sleep(40000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(3333);
        });
    }
}