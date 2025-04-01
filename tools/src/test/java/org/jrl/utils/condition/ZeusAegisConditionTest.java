package org.jrl.utils.condition;

import com.google.common.collect.ImmutableMap;
import org.jrl.tools.condition.JrlCondition;
import org.jrl.tools.condition.JrlConditionItem;
import org.jrl.tools.condition.JrlConditionMatchHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZeusAegisConditionTest {

    @Test
    public void test() {
        JrlCondition condition = JrlCondition.builder()
                .and()
                .item(JrlConditionItem.builder().key("name").values("wb", "wanba").must().build())
                .item(JrlConditionItem.builder().key("age").values(18, 19).build())
                .build();
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, null).isMatch());
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, ImmutableMap.of("a", "b")).isMatch());
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "zeus")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "wanba")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "wb")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "wb", "age", 18)).isMatch());
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "wb", "age", 17)).isMatch());
    }

    @Test
    public void test2() {
        JrlCondition condition = JrlCondition.builder()
                .or()
                .item(JrlConditionItem.builder().key("name").values("wb", "wanba").build())
                .item(JrlConditionItem.builder().key("age").values(18, 19).build())
                .build();

        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, null).isMatch());
        Assertions.assertFalse(JrlConditionMatchHelper.match(condition, ImmutableMap.of("a", "b")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "wb")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "wanba")).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("age", 18)).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("age", 19)).isMatch());
        Assertions.assertTrue(JrlConditionMatchHelper.match(condition, ImmutableMap.of("name", "wb", "age", 18)).isMatch());
    }
}
