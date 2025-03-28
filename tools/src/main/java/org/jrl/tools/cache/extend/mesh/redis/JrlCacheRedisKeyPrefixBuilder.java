package org.jrl.tools.cache.extend.mesh.redis;

import java.util.Objects;

/**
 * 提供redis 继承方式的缓存key生成器（固定前缀拼接模式）
 *
 * @author JerryLong
 */
public class JrlCacheRedisKeyPrefixBuilder<K> extends JrlCacheRedisKeyBuilder {

    private String prefix;
    private K key;

    public JrlCacheRedisKeyPrefixBuilder() {
    }

    public JrlCacheRedisKeyPrefixBuilder(String prefix, K key) {
        this.prefix = prefix;
        this.key = key;
    }

    @Override
    public String build() {
        return prefix + key.toString();
    }

    public String getPrefix() {
        return prefix;
    }

    public K getKey() {
        return key;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setKey(K key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JrlCacheRedisKeyPrefixBuilder)) return false;
        JrlCacheRedisKeyPrefixBuilder<?> that = (JrlCacheRedisKeyPrefixBuilder<?>) o;
        return prefix.equals(that.prefix) && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, key);
    }
}
