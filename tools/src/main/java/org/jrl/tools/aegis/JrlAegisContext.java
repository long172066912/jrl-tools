package org.jrl.tools.aegis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文，用于条件处理
 *
 * @author JerryLong
 */
public class JrlAegisContext {

    private static final ThreadLocal<Context> CONTEXT = ThreadLocal.withInitial(Context::new);

    public static Context getContext() {
        return CONTEXT.get();
    }

    /**
     * 添加上下文
     *
     * @param k
     * @param v
     */
    public static void enter(String k, Object v) {
        getContext().enter(k, v);
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }

    public static class Context {

        private final Map<String, Object> request = new ConcurrentHashMap<>();

        public Map<String, Object> getRequest() {
            return request;
        }

        public Context enter(String k, Object v) {
            request.put(k, v);
            return this;
        }
    }
}
