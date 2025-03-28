package org.jrl.tools.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author JerryLong
 * key构建器
 */
public abstract class JrlCacheKeyBuilder<P, K> {
    /**
     * 获取参数构建信息
     *
     * @return P
     */
    @JsonIgnore
    public abstract P getParam();

    /**
     * 通过参数构建key
     *
     * @return K
     */
    public abstract K build();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JrlCacheKeyBuilder)) return false;
        return build().equals(((JrlCacheKeyBuilder) o).build());
    }
}
