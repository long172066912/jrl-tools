package org.jrl.tools.cache.extend.both;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

/**
 * 多级缓存实现
 *
 * @author JerryLong
 */
public class JrlCacheBothImpl<K, V> extends AbstractJrlBothCache<K, V> {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(JrlCacheBothImpl.class);

    public JrlCacheBothImpl(String name, JrlCache<K, V> meshCache, JrlCache<K, V> localCache) {
        super(name, meshCache, localCache);
    }
}
