package org.jrl.tools.cache.core;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.spi.monitor.JrlCacheMonitorSpi;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.spi.JrlSpiLoader;
import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 缓存代理
 *
 * @author JerryLong
 */
public class JrlCacheProxy<K, V> implements InvocationHandler {

    private static final JrlCacheMonitorSpi MONITOR_SPI = JrlSpiLoader.getInstanceOrDefault(JrlCacheMonitorSpi.class, JrlCacheMonitorSpi.DefaultJrlCacheMonitorSpi::new);
    private static Logger LOGGER = JrlLoggerFactory.getLogger(JrlCacheProxy.class);
    private static final Set<String> NOT_NEED_MONITOR_COMMAND = new HashSet<>(Arrays.asList("getConfig"));

    private JrlCache<K, V> cache;

    public JrlCacheProxy(JrlCache<K, V> cache) {
        this.cache = cache;
    }

    /**
     * 设置缓存实现
     *
     * @param cache
     */
    protected void setCache(JrlCache<K, V> cache) {
        this.cache = cache;
    }

    public JrlCache<K, V> getCache() {
        return cache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long l = System.currentTimeMillis();
        String command = method.getName();
        Throwable ex = null;
        try {
            return method.invoke(cache, args);
        } catch (Throwable e) {
            ex = e;
            LOGGER.error("jrl-cache invoke error ! command : {} , args : {}", method.getName(), JrlJsonNoExpUtil.toJson(args), e);
            throw e;
        } finally {
            if (!NOT_NEED_MONITOR_COMMAND.contains(command)) {
                //监控执行情况
                MONITOR_SPI.monitor(cache.getConfig().getCacheType().name(), cache.getConfig().getCacheType().name(),
                        command, System.currentTimeMillis() - l, ex);
            }
        }
    }
}
