package org.jrl.tools.mock.core;

import org.jrl.tools.mock.JrlMock;
import org.jrl.tools.mock.JrlMockRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mock工厂
 *
 * @author JerryLong
 */
public class JrlMockRuleFactory {
    /**
     * mock规则
     */
    private static final Map<Class<?>, Map<String, JrlMockRule>> MOCK_RULE_MAP = new ConcurrentHashMap<>(16);

    /**
     * 获取mock规则
     *
     * @param clazz      类
     * @param methodName 方法名
     * @return mock规则
     */
    public static JrlMockRule getMockRule(Class<?> clazz, String methodName) {
        if (!JrlMock.isOpen()) {
            return null;
        }
        Map<String, JrlMockRule> mockRuleMap = MOCK_RULE_MAP.get(clazz);
        if (mockRuleMap == null) {
            return null;
        }
        JrlMockRule mockRule = mockRuleMap.get(methodName);
        if (mockRule == null) {
            return null;
        }
        return mockRule;
    }

    /**
     * 添加mock规则
     *
     * @param clazz      类
     * @param methodName 方法名
     * @param mockRule   mock规则
     */
    public static void addMockRule(Class<?> clazz, String methodName, JrlMockRule mockRule) {
        MOCK_RULE_MAP.computeIfAbsent(clazz, e -> new ConcurrentHashMap<>(8)).put(methodName, mockRule);
    }

    /**
     * 移除mock规则
     *
     * @param clazz  类
     * @param method 方法名
     */
    public static void removeMockRule(Class<?> clazz, String method) {
        MOCK_RULE_MAP.computeIfAbsent(clazz, e -> new ConcurrentHashMap<>(8)).remove(method);
    }
}
