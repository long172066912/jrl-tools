package org.jrl.tools.cache.extend.local;

/**
* 本地缓存value包装
* @author JerryLong
*/
public class JrlCacheData<V> {
    /**
     * 数据
     */
    private V data;

    public JrlCacheData(V data) {
        this.data = data;
    }

    public V getData() {
        return data;
    }

    public void setData(V data) {
        this.data = data;
    }
}
