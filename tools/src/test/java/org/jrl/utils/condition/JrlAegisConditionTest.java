package org.jrl.utils.condition;

import com.google.common.collect.ImmutableMap;
import org.jrl.tools.condition.JrlCondition;
import org.jrl.tools.condition.JrlConditionItem;
import org.jrl.tools.condition.JrlConditionMatchHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JrlAegisConditionTest {

    @Test
    public void test() {
        JrlCondition condition = JrlCondition.builder()
                .and()
                .item(JrlConditionItem.builder().key("name").values("bb", "aaa").must().build())
                .item(JrlConditionItem.builder().key("age").values(18, 19).build())
                .build();
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, null).isMatch());
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, ImmutableMap.of("a", "b")).isMatch());
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "jrl")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "aaa")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "bb")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "bb", "age", 18)).isMatch());
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "bb", "age", 17)).isMatch());
    }

    @Test
    public void test2() {
        JrlCondition condition = JrlCondition.builder()
                .or()
                .item(JrlConditionItem.builder().key("name").values("aa", "bbb").build())
                .item(JrlConditionItem.builder().key("age").values(18, 19).build())
                .build();

        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, null).isMatch());
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, ImmutableMap.of("a", "b")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "aa")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "bbb")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("age", 18)).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("age", 19)).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "aa", "age", 18)).isMatch());
    }
}
