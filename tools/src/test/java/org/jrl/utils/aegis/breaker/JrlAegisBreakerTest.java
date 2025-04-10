package org.jrl.utils.aegis.breaker;

import org.jrl.tools.aegis.JrlAegis;
import org.jrl.tools.aegis.builder.JrlAegisBuilder;
import org.jrl.tools.aegis.core.JrlAegisEntry;
import org.jrl.tools.aegis.core.rule.JrlAegisBreakerRule;
import org.jrl.tools.aegis.exception.JrlAegisBreakerException;
import org.jrl.tools.aegis.manager.JrlAegisManager;
import org.jrl.tools.utils.function.AbstractJrlCallable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class JrlAegisBreakerTest {

    @Test
    public void exceptionCount() throws InterruptedException {
        final JrlAegis aegis = JrlAegisBuilder.breaker("exceptionCount")
                .addRule(JrlAegisBreakerRule.builder().exceptionCount().minExceptionCount(1).timeWindow(1).stats(1000, 10).build())
                .build();
        for (int i = 0; i < 2; i++) {
            Assertions.assertThrows(RuntimeException.class, () -> aegis.tryAcquire(new AbstractJrlCallable<Object>() {
                @Override
                public Object call() throws Exception {
                    throw new RuntimeException();
                }
            }));
        }
        for (int i = 0; i < 8; i++) {
            Assertions.assertDoesNotThrow(() -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 1;
                }
            }));
        }

        Assertions.assertThrows(JrlAegisBreakerException.class, () -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }));
        Thread.sleep(1000L);
        //已恢复
        Assertions.assertDoesNotThrow(() -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }));
        Assertions.assertThrows(RuntimeException.class, () -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }));
    }

    @Test
    public void exceptionRadio() {
        final JrlAegis aegis = JrlAegisBuilder.breaker("exceptionRadio")
                .addRule(JrlAegisBreakerRule.builder().exceptionRadio().minExceptionRadio(0.5).stats(1000, 100).build())
                .build();
        for (int i = 0; i < 51; i++) {
            Assertions.assertThrows(RuntimeException.class, () -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 1;
                }
            }));
        }

        for (int i = 0; i < 49; i++) {
            Assertions.assertDoesNotThrow(() -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 1;
                }
            }));
        }
        Assertions.assertThrows(JrlAegisBreakerException.class, () -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }));
    }

    @Test
    public void slowRadio() {
        final JrlAegis aegis = JrlAegisBuilder.breaker("slowRadio")
                .addRule(JrlAegisBreakerRule.builder().slowRadio().minSlowRadio(0.5).minCallMs(100).stats(2000, 10).build())
                .build();

        for (int i = 0; i < 4; i++) {
            Assertions.assertDoesNotThrow(() -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 1;
                }
            }));
        }
        for (int i = 0; i < 6; i++) {
            Assertions.assertDoesNotThrow(() -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                    }
                    return 1;
                }
            }));
        }
        Assertions.assertThrows(JrlAegisBreakerException.class, () -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }));
    }

    @Test
    public void degrade() {
        final JrlAegis aegis = JrlAegisBuilder.breaker("degrade")
                .addRule(JrlAegisBreakerRule.builder().exceptionCount().minExceptionCount(1).timeWindow(1).stats(1000, 10).priority(1).build())
                .build();

        Assertions.assertDoesNotThrow(() -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }));
        //添加降级
        final JrlAegisBreakerRule degrade = JrlAegisBreakerRule.builder().degrade().priority(2).build();

        JrlAegisManager.getAegis("degrade").addRule(degrade);

        Assertions.assertThrows(JrlAegisBreakerException.class, () -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }));
        //删除降级，回归异常熔断
        JrlAegisManager.getAegis("degrade").deleteRule(degrade);

        for (int i = 0; i < 2; i++) {
            Assertions.assertThrows(RuntimeException.class, () -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    throw new RuntimeException();
                }
            }));
        }
        for (int i = 0; i < 7; i++) {
            Assertions.assertDoesNotThrow(() -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 1;
                }
            }));
        }

        Assertions.assertThrows(JrlAegisBreakerException.class, () -> aegis.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }));
    }

    @Test
    public void async() throws InterruptedException {
        final JrlAegis aegis = JrlAegisBuilder.breaker("async")
                .addRule(JrlAegisBreakerRule.builder().exceptionCount().minExceptionCount(1).timeWindow(1).stats(1000, 10).build())
                .build();

        List<JrlAegisEntry> entryList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entryList.add(aegis.tryEntry());
        }
        Thread.sleep(1000L);
        for (int i = 0; i < entryList.size(); i++) {
            if (i < 2) {
                entryList.get(i).end(new RuntimeException());
            } else {
                entryList.get(i).end();
            }
        }

        Assertions.assertThrows(JrlAegisBreakerException.class, aegis::tryEntry);
        Thread.sleep(1000L);
        //已恢复
        Assertions.assertDoesNotThrow(() -> aegis.tryEntry());
    }

    @Test
    public void async1() throws InterruptedException {
        final JrlAegis aegis = JrlAegisBuilder.breaker("async1")
                .addRule(JrlAegisBreakerRule.builder().exceptionCount().minExceptionCount(1).timeWindow(1).stats(1000, 10).build())
                .build();

        List<JrlAegisEntry> entryList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            entryList.add(aegis.tryEntry(new AbstractJrlCallable<Object>() {
                @Override
                public Object call() throws Exception {
                    throw new RuntimeException();
                }
            }));
        }
        for (int i = 0; i < 8; i++) {
            entryList.add(aegis.tryEntry(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 1;
                }
            }));
        }
        Thread.sleep(1000L);
        for (JrlAegisEntry zeusAegisEntry : entryList) {
            zeusAegisEntry.end();
        }

        Assertions.assertThrows(JrlAegisBreakerException.class, aegis::tryEntry);
        Thread.sleep(1000L);
        //已恢复
        Assertions.assertDoesNotThrow(() -> aegis.tryEntry());
    }
}
