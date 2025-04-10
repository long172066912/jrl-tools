package org.jrl.tools.aegis.core;

import org.jrl.tools.aegis.JrlAegisRule;
import org.jrl.tools.aegis.model.JrlAegisScope;

import java.util.List;
import java.util.Map;

/**
 * 控制接口
 *
 * @author JerryLong
 */
public interface JrlAegisControl<R extends JrlAegisRule> {

    /**
     * 获取所有规则
     *
     * @return
     */
    Map<JrlAegisScope, List<R>> getRules();

    /**
     * 更新规则，全量替换
     *
     * @param rules 新的规则
     */
    void changeRules(List<R> rules);

    /**
     * 新增规则
     *
     * @param rule
     */
    void addRule(R rule);

    /**
     * 删除规则
     *
     * @param rule
     */
    void deleteRule(R rule);

    /**
     * 更新规则
     *
     * @param rule
     */
    void changeRule(R rule);
}
