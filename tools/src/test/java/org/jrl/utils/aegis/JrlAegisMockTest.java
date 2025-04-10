package org.jrl.utils.aegis;

import com.google.common.collect.ImmutableMap;
import org.jrl.tools.aegis.JrlAegis;
import org.jrl.tools.aegis.JrlAegisUtil;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.exception.JrlAegisLimitException;
import org.jrl.tools.aegis.manager.JrlAegisManager;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.mock.JrlMock;
import org.jrl.tools.utils.function.AbstractJrlCallable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JrlAegisMockTest {

    @Test
    public void testMock() throws InterruptedException {
        final JrlAegis limiter = JrlAegisUtil.limit().local("testMock")
                .addRule(JrlAegisLimitRule.builder().count(10).build())
                .build();

        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertThrows(JrlAegisLimitException.class, () -> limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 2;
            }
        }));
        JrlAegisManager.getAegis("testMock").openBlockMock();
        Assertions.assertThrows(JrlAegisLimitException.class, () -> limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 2;
            }
        }));
        JrlMock.doReturn(1).when(JrlAegis.class).time(1, 2, TimeUnit.MILLISECONDS).func("testMock");
        Assertions.assertEquals(1, limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 2;
            }
        }));
        JrlMock.doThrow(new RuntimeException("testMock")).when(JrlAegis.class).time(1, 2, TimeUnit.MILLISECONDS).func("testMock");
        Assertions.assertThrows(RuntimeException.class, () -> limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 2;
            }
        }));
        JrlAegisManager.getAegis("testMock").closeBlockMock();
        Assertions.assertThrows(JrlAegisLimitException.class, () -> limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 2;
            }
        }));
    }

    @Test
    public void testMockObj() throws InterruptedException {
        final JrlAegis zeusAegis = JrlAegisUtil.limitQps("testMockObj", 1);
        zeusAegis.openBlockMock();
        JrlMock.doReturn(new TestMock<>("test", 18, 1, ImmutableMap.of("aa", "bb"))).when(JrlAegis.class).time(1, 2, TimeUnit.MILLISECONDS).func("testMockObj");
        zeusAegis.tryAcquire();
        final TestMock<Integer, String> test = zeusAegis.tryAcquire(new AbstractJrlCallable<TestMock<Integer, String>>() {
            @Override
            public TestMock<Integer, String> call() throws Exception {
                throw new RuntimeException("testMockObj");
            }
        });
        Assertions.assertNotNull(test);
        Assertions.assertEquals("test", test.getName());
        Assertions.assertEquals(18, test.getAge());
        Assertions.assertEquals(1, test.getData());
        Assertions.assertEquals(ImmutableMap.of("aa", "bb"), test.getMap());

        //关闭mock
        zeusAegis.closeBlockMock();
        Assertions.assertFalse(zeusAegis.tryAcquire());
    }

    @Test
    public void testMockJson() throws InterruptedException {
        final JrlAegis zeusAegis = JrlAegisUtil.limitQps("testMockObj", 1);
        zeusAegis.openBlockMock();
        JrlMock.doReturn(JrlJsonNoExpUtil.toJson(new TestMock<>("test", 18, 1, ImmutableMap.of("aa", "bb")))).when(JrlAegis.class).time(1, 2, TimeUnit.MILLISECONDS).func("testMockObj");
        zeusAegis.tryAcquire();
        final TestMock<Integer, String> test = zeusAegis.tryAcquire(new AbstractJrlCallable<TestMock<Integer, String>>() {
            @Override
            public TestMock<Integer, String> call() throws Exception {
                throw new RuntimeException("testMockObj");
            }
        });
        Assertions.assertNotNull(test);
        Assertions.assertEquals("test", test.getName());
        Assertions.assertEquals(18, test.getAge());
        Assertions.assertEquals(1, test.getData());
        Assertions.assertEquals(ImmutableMap.of("aa", "bb"), test.getMap());

        //关闭mock
        zeusAegis.closeBlockMock();
        Assertions.assertFalse(zeusAegis.tryAcquire());
    }

    private static class TestMock<T, V> {
        private String name;
        private int age;
        private T data;
        private Map<String, V> map;

        public TestMock() {
        }

        public TestMock(String name, int age, T data, Map<String, V> map) {
            this.name = name;
            this.age = age;
            this.data = data;
            this.map = map;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public Map<String, V> getMap() {
            return map;
        }

        public void setMap(Map<String, V> map) {
            this.map = map;
        }
    }
}
