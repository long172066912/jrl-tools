package org.jrl.utils.mock;

import org.jrl.tools.mock.model.MockResponse;
import org.jrl.tools.mock.JrlMock;
import org.jrl.tools.mock.JrlMockUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class MockTest {

    @Test
    public void mockTest() throws Exception {
        JrlMock.open();
        JrlMock.doReturn("bbb").when(MockTestModel.class).func("getName");
        final MockTestModel model = new MockTestModel("aaa", 1);
        Assertions.assertEquals("bbb", model.getName());
        Assertions.assertEquals(1, model.getAge());
    }


    @Test
    public void mockTimeTest() throws Exception {
        JrlMock.open();
        final long l = System.currentTimeMillis();
        JrlMock.doReturn("bbb").when(MockTestModel.class).time(100, 200, TimeUnit.MILLISECONDS).func("getName");
        final MockTestModel model = new MockTestModel("aaa", 1);
        Assertions.assertEquals("bbb", model.getName());
        Assertions.assertEquals(1, model.getAge());
        Assertions.assertTrue(System.currentTimeMillis() - l >= 100);
        Assertions.assertTrue(System.currentTimeMillis() - l <= 200);
    }

    @Test
    public void mockThrowTest() throws Exception {
        JrlMock.open();
        JrlMock.doThrow(new RuntimeException("test")).when(MockTestModel.class).func("getName");
        final MockTestModel model = new MockTestModel("aaa", 1);
        Assertions.assertThrows(RuntimeException.class, model::getName);
        Assertions.assertEquals(1, model.getAge());
    }

    @Test
    public void mockNothingTest() throws Exception {
        JrlMock.open();
        JrlMock.doNothing().when(MockTestModel.class).func("getName");
        final MockTestModel model = new MockTestModel("aaa", 1);
        Assertions.assertNull(model.getName());
        Assertions.assertEquals(1, model.getAge());
    }

    @Test
    public void mockNothingTest1() throws Exception {
        JrlMock.open();
        JrlMock.doNothing().when(MockTestModel.class).func("getName", "run");
        final MockTestModel model = new MockTestModel("aaa", 1);
        Assertions.assertNull(model.getName());
        AtomicInteger atomicInteger = new AtomicInteger(0);
        model.run(atomicInteger::incrementAndGet);
        Assertions.assertEquals(0, atomicInteger.get());
        Assertions.assertEquals(1, model.getAge());
    }

    @Test
    public void mockRunReturnTest() throws Exception {
        JrlMock.open();
        JrlMock.doRunReturn(objects -> {
            for (Object object : objects) {
                System.out.println("arg : " + object);
            }
            return "run";
        }).when(MockTestModel.class).func("testRun");

        final MockTestModel model = new MockTestModel("aaa", 1);
        Assertions.assertEquals("run", model.testRun("aaa", "bbb"));
    }


    private static class MockTestModel implements Serializable {
        private final String name;
        private final int age;

        public MockTestModel(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return JrlMockUtil.mock(MockTestModel.class, "getName", () -> name);
        }

        public int getAge() {
            return JrlMockUtil.mock(MockTestModel.class, "getAge", () -> age);
        }

        public String testRun(Object... args) {
            return JrlMockUtil.mock(MockTestModel.class, "testRun", () -> name, args);
        }

        void run(Runnable runnable) {
            final MockResponse<Object> run = JrlMockUtil.mock(MockTestModel.class, "run");
            if (!run.isMock()) {
                runnable.run();
            }
        }
    }
}