package org.jrl.tools.mock;

import org.jrl.tools.mock.core.JrlMockRuleFactory;
import org.jrl.tools.mock.model.MockResponse;

import java.util.function.Supplier;

/**
 * mock功能帮助类，用于mock对象，并拿到mock结果
 *
 * @author JerryLong
 */
public class JrlMockUtil {
    /**
     * mock对象与方法
     *
     * @param clazz      对象
     * @param methodName 方法名
     * @param <T>        返回对象类型
     * @return {@link MockResponse}
     */
    public static <T> MockResponse<T> mock(Class<?> clazz, String methodName) {
        return getMockResponse(JrlMockRuleFactory.getMockRule(clazz, methodName));
    }

    /**
     * mock对象与方法
     *
     * @param clazz      对象
     * @param methodName 方法名
     * @param callable   待mock方法
     * @param <T>        返回对象类型
     * @return {@link MockResponse}
     */
    public static <T> T mock(Class<?> clazz, String methodName, Supplier<T> callable) {
        final MockResponse<T> mockResponse = getMockResponse(JrlMockRuleFactory.getMockRule(clazz, methodName));
        if (mockResponse.isMock()) {
            return mockResponse.getResult();
        }
        return callable.get();
    }

    /**
     * mock对象与方法
     *
     * @param clazz      对象
     * @param methodName 方法名
     * @param callable   待mock方法
     * @param <T>        返回对象类型
     * @param args       参数
     * @return {@link MockResponse}
     */
    public static <T> T mock(Class<?> clazz, String methodName, Supplier<T> callable, Object... args) {
        final MockResponse<T> mockResponse = getMockResponse(JrlMockRuleFactory.getMockRule(clazz, methodName), args);
        if (mockResponse.isMock()) {
            return mockResponse.getResult();
        }
        return callable.get();
    }

    private static <T> MockResponse<T> getMockResponse(JrlMockRule mockRule, Object... args) {
        if (null == mockRule) {
            return MockResponse.noMock();
        }
        mockRule.sleep();
        if (null != mockRule.getDoNothing() && mockRule.getDoNothing()) {
            return MockResponse.mock(null);
        }
        if (null != mockRule.getDoThrow()) {
            return MockResponse.mock(mockRule.getDoThrow());
        }
        if (null != mockRule.getDoRunReturn()) {
            return (MockResponse<T>) MockResponse.mock(mockRule.getDoRunReturn().apply(args));
        }
        return (MockResponse<T>) MockResponse.mock(mockRule.getDoReturn());
    }
}
