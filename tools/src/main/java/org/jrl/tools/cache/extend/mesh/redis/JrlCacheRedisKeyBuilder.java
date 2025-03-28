package org.jrl.tools.cache.extend.mesh.redis;

import org.jrl.tools.cache.JrlCacheKeyBuilder;

/**
 * 提供redis 继承方式的缓存key生成器
 *
 * @author JerryLong
 */
public abstract class JrlCacheRedisKeyBuilder extends JrlCacheKeyBuilder<Void, String> {

    @Override
    public Void getParam() {
        return null;
    }
}
