package org.jrl.tools.mock.core;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * mock规则构建器
 *
 * @author JerryLong
 */
public class JrlMockRuleBuilder {

    public static DoSomething doSomething() {
        return new DoSomething();
    }

    public static class DoSomething {

        protected DoSomething() {

        }

        public WhenClass doReturn(Object obj) {
            return new WhenClass(obj);
        }

        public WhenClass doRunReturn(Function<Object[], Object> obj) {
            return new WhenClass(obj);
        }

        public WhenClass doThrow(Throwable throwable) {
            return new WhenClass(throwable);
        }

        public WhenClass doNothing() {
            return new WhenClass();
        }
    }

    public static class WhenClass {
        private Object doReturn;
        private Function<Object[], Object> doRunReturn;
        private Throwable doThrow;
        private Boolean doNothing;

        protected WhenClass(Object doReturn) {
            this.doReturn = doReturn;
        }

        protected WhenClass(Function<Object[], Object> doRunReturn) {
            this.doRunReturn = doRunReturn;
        }

        protected WhenClass(Throwable doThrow) {
            this.doThrow = doThrow;
        }

        protected WhenClass() {
            this.doNothing = true;
        }

        public WhenMethod when(Class<?> clazz) {
            return new WhenMethod(doReturn, doRunReturn, doThrow, doNothing, clazz);
        }
    }

    public static class WhenMethod {
        private Object doReturn;
        private Function<Object[], Object> doRunReturn;
        private Throwable doThrow;
        private Boolean doNothing;
        private Class<?> clazz;
        private Integer min;
        private Integer max;
        private TimeUnit timeUnit;

        public WhenMethod(Object doReturn, Function<Object[], Object> doRunReturn, Throwable doThrow, Boolean doNothing, Class<?> clazz) {
            this.doReturn = doReturn;
            this.doRunReturn = doRunReturn;
            this.doThrow = doThrow;
            this.doNothing = doNothing;
            this.clazz = clazz;
        }

        /**
         * 延迟执行
         *
         * @param min      最小延迟
         * @param max      最大延迟
         * @param timeUnit 时间单位
         * @return WhenMethod
         */
        public WhenMethod time(int min, int max, TimeUnit timeUnit) {
            this.min = min;
            this.max = max;
            this.timeUnit = timeUnit;
            return this;
        }

        public void func(String... methods) {
            new DefaultJrlMockRule(doReturn, doRunReturn, doThrow, doNothing, clazz, methods, min, max, timeUnit).open();
        }
    }
}
