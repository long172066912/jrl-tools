package org.jrl.tools.mock.core;

import org.jrl.tools.mock.JrlMockRule;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * mock规则处理类
 *
 * @author JerryLong
 */
public class DefaultJrlMockRule implements JrlMockRule {
    private Object doReturn;
    private Function<Object[], Object> doRun;
    private Throwable doThrow;
    private Boolean doNothing;
    private Class<?> clazz;
    private String[] methods;
    private Integer min;
    private Integer max;
    private TimeUnit timeUnit;

    public DefaultJrlMockRule(Object doReturn, Function<Object[], Object> doRunReturn, Throwable doThrow, Boolean doNothing, Class<?> clazz, String[] methods, Integer min, Integer max, TimeUnit timeUnit) {
        this.doReturn = doReturn;
        this.doRun = doRunReturn;
        this.doThrow = doThrow;
        this.doNothing = doNothing;
        this.clazz = clazz;
        this.methods = methods;
        this.min = min;
        this.max = max;
        this.timeUnit = timeUnit;
    }

    @Override
    public void open() {
        //注册到工厂中
        for (String method : methods) {
            JrlMockRuleFactory.addMockRule(clazz, method, this);
        }
    }

    @Override
    public void close() {
        //从工厂中删除
        for (String method : methods) {
            JrlMockRuleFactory.removeMockRule(clazz, method);
        }
    }

    @Override
    public JrlMockRule doReturn(Object obj) {
        this.doReturn = obj;
        return this;
    }

    @Override
    public JrlMockRule doRunReturn(Function<Object[], Object> obj) {
        this.doRun = obj;
        return this;
    }

    @Override
    public JrlMockRule doThrow(Throwable throwable) {
        this.doThrow = throwable;
        return this;
    }

    @Override
    public JrlMockRule doNothing() {
        this.doNothing = true;
        return this;
    }

    @Override
    public JrlMockRule when(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }

    @Override
    public JrlMockRule when(String... methods) {
        this.methods = methods;
        return this;
    }

    @Override
    public JrlMockRule time(int min, int max, TimeUnit timeUnit) {
        this.min = min;
        this.max = max;
        this.timeUnit = timeUnit;
        return this;
    }

    @Override
    public Object getDoReturn() {
        return doReturn;
    }

    @Override
    public Function<Object[], Object> getDoRunReturn() {
        return doRun;
    }

    @Override
    public Throwable getDoThrow() {
        return doThrow;
    }

    @Override
    public Boolean getDoNothing() {
        return doNothing;
    }

    @Override
    public void sleep() {
        if (null != min && null != max && 0 < max) {
            try {
                timeUnit.sleep(ThreadLocalRandom.current().nextInt(Math.min(min, max), Math.max(min, max)));
            } catch (InterruptedException e) {
            }
        }
    }
}
