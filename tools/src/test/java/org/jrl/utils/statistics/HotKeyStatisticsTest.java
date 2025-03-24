package org.jrl.utils.statistics;

import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.utils.hotkey.AbstractJrlHotKeyStatistics;
import org.jrl.tools.utils.hotkey.DefaultJrlHotKey;
import org.jrl.tools.utils.hotkey.JrlHotKey;
import org.jrl.tools.utils.hotkey.JrlHotKeyPriorityQueueStatistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * 热key统计测试
 *
 * @author JerryLong
 */
public class HotKeyStatisticsTest {

    @Test
    public void priorityQueueTest() {
        final JrlHotKeyPriorityQueueStatistics<String> statistics = new JrlHotKeyPriorityQueueStatistics<>(50, 100, DefaultJrlHotKey::new);
        final long l = System.currentTimeMillis();
        final List<JrlHotKey<String>> hotKeys = test(statistics, i -> "key" + i, 100000);
        System.out.println(JrlJsonNoExpUtil.toJson(hotKeys));
        Assertions.assertEquals(50, hotKeys.size());
        System.out.println(System.currentTimeMillis() - l);
        hotKeys.sort((o1, o2) -> o2.intValue() - o1.intValue());

        for (int i = 1; i < hotKeys.get(0).intValue(); i++) {
            statistics.hotKeyIncr("test");
        }
        final List<JrlHotKey<String>> hotkeys1 = statistics.getHotKeysAndClean();
        hotkeys1.sort((o1, o2) -> o2.intValue() - o1.intValue());

        Assertions.assertEquals(hotKeys.get(0).intValue(), hotkeys1.get(0).intValue());

        Assertions.assertTrue(statistics.getHotkeys().isEmpty());
    }

    @Test
    public void ModelTest() {
        final JrlHotKeyPriorityQueueStatistics<ModelKey> statistics = new JrlHotKeyPriorityQueueStatistics<>(50, 100, DefaultJrlHotKey::new);
        final String cacheType = "test";
        final List<JrlHotKey<ModelKey>> hotKeys = test(statistics, i -> new ModelKey(cacheType, "key" + i), 100000);
        Assertions.assertEquals(50, hotKeys.size());
    }

    private <K> List<JrlHotKey<K>> test(AbstractJrlHotKeyStatistics<K> statistics, Function<Integer, K> keySupplier, int num) {
        for (int i = 0; i < num; i++) {
            final int i1 = ThreadLocalRandom.current().nextInt(0, 100);
            statistics.hotKeyIncr(keySupplier.apply(i1));
        }
        return statistics.getHotkeys();
    }

    private static class ModelKey {
        private String cacheType;
        private String key;

        public ModelKey(String cacheType, String key) {
            this.cacheType = cacheType;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModelKey)) return false;
            ModelKey modelKey = (ModelKey) o;
            return Objects.equals(cacheType, modelKey.cacheType) && Objects.equals(key, modelKey.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cacheType, key);
        }
    }
}
