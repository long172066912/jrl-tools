package org.jrl.tools.thread.core.factory;

import java.util.concurrent.ExecutorService;

/**
 * 构建线程池帮助类
 *
 * @author JerryLong
 */
public class JrlThreadPoolBuilder {

    /**
     * 使用默认配置构建线程池
     *
     * @param name 线程池名称
     * @return {@link Builder}
     */
    public static Builder builder(String name) {
        return new Builder(name, JrlThreadPoolConfig.builder().build());
    }

    /**
     * 使用自定义配置构建线程池
     *
     * @param name   线程池名称
     * @param config 配置
     * @return {@link Builder}
     */
    public static Builder builder(String name, JrlThreadPoolConfig config) {
        return new Builder(name, config);
    }

    /**
     * 构建线程池
     */
    public static class Builder {
        private String name;
        private JrlThreadPoolConfig config;

        public Builder(String name, JrlThreadPoolConfig executor) {
            this.name = name;
            this.config = executor;
        }

        /**
         * 相同名称的线程池不能重复
         *
         * @param <T> 线程池类型
         * @return 线程池 {@link JrlThreadPool}
         */
        public <T extends ExecutorService> T build() {
            return JrlThreadPoolFactory.computeIfAbsent(this.name, this.config);
        }
    }
}
