package org.jrl.tools.cache.hotkey;

import org.apache.commons.collections4.CollectionUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.utils.hotkey.AbstractJrlHotKeyStatistics;
import org.jrl.tools.utils.hotkey.JrlHotKey;
import org.jrl.tools.utils.hotkey.JrlHotKeyPriorityQueueStatistics;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JrlCache热key统计
 *
 * @author JerryLong
 */
public class JrlCacheHotKeyStatistics<K> {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(JrlCacheHotKeyStatistics.class);

    /**
     * 热key统计
     */
    private final AbstractJrlHotKeyStatistics<K> hotKeyStatistics;
    private final ScheduledExecutorService executorService;
    private final JrlCacheHotKeyConfig config;
    private final AtomicBoolean isStat = new AtomicBoolean(false);
    private final Consumer<Set<K>> hotKeyConsumer;

    public JrlCacheHotKeyStatistics(String cacheName, JrlCacheHotKeyConfig config, Function<K, JrlCacheHotKey<K>> hotKeySupplier, Consumer<Set<K>> hotKeyConsumer) {
        LOGGER.info("jrl-cache hotKeyStatistics init cacheName : {} , config : {}", cacheName, JrlJsonNoExpUtil.toJson(config));
        this.config = config;
        hotKeyStatistics = new JrlHotKeyPriorityQueueStatistics<>(config.getCapacity(), config.getCountLeastValue(), hotKeySupplier::apply);
        this.hotKeyConsumer = hotKeyConsumer;
        executorService = JrlThreadUtil.newSchedulePool("jrl-cache-hotkey-statistics", 1);
        this.stat(this.config.getStatSeconds());
    }

    public void stat(int statSeconds) {
        if (!config.isStatHotKey()) {
            return;
        }
        if (isStat.compareAndSet(false, true)) {
            executorService.scheduleAtFixedRate(() -> {
                try {
                    final List<JrlHotKey<K>> hotKeys = hotKeyStatistics.getHotKeysAndClean();
                    if (CollectionUtils.isNotEmpty(hotKeys)) {
                        LOGGER.info("jrl-cache hotKeyStatistics getHotKeysAndClean : {}", JrlJsonNoExpUtil.toJson(hotKeys));
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("jrl-cache hotKeyStatistics getHotKeysAndClean : {}", JrlJsonNoExpUtil.toJson(hotKeys));
                        }
                        //发送事件
                        if (config.isAutoCacheHotKey()) {
                            this.hotKeyConsumer.accept(hotKeys.stream().map(JrlHotKey::getKey).collect(Collectors.toSet()));
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.warn("jrl-cache hotKeyStatistics getHotKeysAndClean error ! ", e);
                }
            }, statSeconds, statSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * 增加key的统计
     *
     * @param key key
     */
    public void incr(K key) {
        if (!config.isStatHotKey()) {
            return;
        }
        hotKeyStatistics.hotKeyIncr(key);
    }

    public JrlCacheHotKeyConfig getConfig() {
        return config;
    }

    public void setCapacity(int capacity, int countLeastValue, int localCacheSeconds) {
        this.config.setCapacity(capacity);
        this.hotKeyStatistics.setCapacity(capacity);
        this.config.setCountLeastValue(countLeastValue);
        this.hotKeyStatistics.setCountLeastValue(countLeastValue);
        this.config.setLocalCacheSeconds(localCacheSeconds);
    }
}
