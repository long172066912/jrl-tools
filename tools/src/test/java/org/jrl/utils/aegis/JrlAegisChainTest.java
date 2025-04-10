package org.jrl.utils.aegis;

import org.jrl.tools.aegis.JrlAegis;
import org.jrl.tools.aegis.JrlAegisUtil;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.model.JrlAegisScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JrlAegisChainTest {

    @Test
    public void test() {
        //定义3个作用域级别，优先级越高越先执行
        final JrlAegis limiter = JrlAegisUtil.limit().local("test")
                // 作用域级别1
                .addRule(JrlAegisLimitRule.builder().id(3).scope(new JrlAegisScope(1, "兜底")).build())
                .addRule(JrlAegisLimitRule.builder().id(2).scope(new JrlAegisScope(1, "兜底")).build())
                .addRule(JrlAegisLimitRule.builder().id(1).scope(new JrlAegisScope(1, "兜底")).build())
                // 作用域级别2
                .addRule(JrlAegisLimitRule.builder().id(1).scope(new JrlAegisScope(2, "scope2")).build())
                .addRule(JrlAegisLimitRule.builder().id(2).scope(new JrlAegisScope(2, "scope2")).build())
                .addRule(JrlAegisLimitRule.builder().id(3).scope(new JrlAegisScope(2, "scope2")).build())
                // 作用域级别3
                .addRule(JrlAegisLimitRule.builder().id(4).scope(new JrlAegisScope(999, "scope3")).build())
                .addRule(JrlAegisLimitRule.builder().id(5).scope(new JrlAegisScope(999, "scope3")).build())
                .addRule(JrlAegisLimitRule.builder().id(6).scope(new JrlAegisScope(999, "scope3")).build())
                .build();
        //在chain里打日志看
        Assertions.assertTrue(limiter.tryAcquire());
    }

}
