package org.jrl.tools.mock;

import org.jrl.tools.mock.core.JrlMockRuleBuilder;

import java.util.function.Function;

/**
 * mock api，用来定义mock规则
 *
 * @author JerryLong
 */
public class JrlMock {
    /**
     * 全局开关
     */
    private static Boolean isOpen = false;

    public static Boolean isOpen() {
        return isOpen;
    }

    /**
     * 开启mock规则
     */
    public static void open() {
        isOpen = true;
    }
//
//    /**
//     * 关闭mock规则
//     */
//    public static void close() {
//        isOpen = false;
//    }

    /**
     * 设置mock返回值
     *
     * @param obj 返回对象
     * @return {@link JrlMockRuleBuilder.WhenClass}
     */
    public static JrlMockRuleBuilder.WhenClass doReturn(Object obj) {
        return JrlMockRuleBuilder.doSomething().doReturn(obj);
    }

    /**
     * 设置mock异常
     *
     * @param throwable 异常
     * @return {@link JrlMockRuleBuilder.WhenClass}
     */
    public static JrlMockRuleBuilder.WhenClass doThrow(Throwable throwable) {
        return JrlMockRuleBuilder.doSomething().doThrow(throwable);
    }

    /**
     * 设置mock不执行
     *
     * @return {@link JrlMockRuleBuilder.WhenClass}
     */
    public static JrlMockRuleBuilder.WhenClass doNothing() {
        return JrlMockRuleBuilder.doSomething().doNothing();
    }

    /**
     * 设置mock执行，返回值通过run函数计算
     *
     * @param run 函数
     * @return {@link JrlMockRuleBuilder.WhenClass}
     */
    public static JrlMockRuleBuilder.WhenClass doRunReturn(Function<Object[], Object> run) {
        return JrlMockRuleBuilder.doSomething().doRunReturn(run);
    }
}
