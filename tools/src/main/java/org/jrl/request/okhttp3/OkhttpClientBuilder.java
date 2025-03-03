package org.jrl.request.okhttp3;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpConnectionPoolMetrics;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jrl.log.JrlLoggerFactory;
import org.jrl.request.okhttp3.monitor.JrlOkHttp3MetricsEventListener;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* client构建工具
* @author JerryLong
*/
public class OkhttpClientBuilder {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(OkhttpClientBuilder.class);

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        /**
         * 连接池名称
         */
        private String name;
        /**
         * 连接超时时间，单位：秒
         */
        private Integer connectTimeoutSeconds = 10;
        /**
         * 请求超时时间，单位：秒
         */
        private Integer writeTimeoutSeconds = 10;
        /**
         * 读取超时时间，单位：秒
         */
        private Integer readTimeoutSeconds = 30;
        /**
         * 最大连接数
         */
        private int maxIdleConnections = 200;
        /**
         * 连接保持时间，单位：秒
         */
        private int keepAliveSeconds = 300;
        /**
         * 重试控制
         */
        private AbstractRetryController retryController = new AbstractRetryController.DefaultRetryController();
        /**
         * 在连接失败的情况下是否重试，默认：不重试，通过重试控制进行重试
         */
        private boolean retryOnConnectionFailure = false;
        /**
         * 是否连接池方式
         */
        private boolean useConnectionPool = true;
        /**
         * 预热处理
         */
        private BasePreheatHandler preheatHandler = null;
        /**
         * 应用拦截器
         */
        private List<Interceptor> interceptors = new ArrayList<>();
        /**
         * 网络拦截器
         */
        private List<Interceptor> networkInterceptors = new ArrayList<>();
        /**
         * 时间单位，默认毫秒
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        /**
         * 是否开启监控，默认开启
         * 连接池监控默认开启，暂不支持关闭
         */
        private boolean monitor = true;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder connectTimeoutSeconds(int connectTimeoutSeconds) {
            this.connectTimeoutSeconds = connectTimeoutSeconds;
            return this;
        }

        public Builder writeTimeoutSeconds(int writeTimeoutSeconds) {
            this.writeTimeoutSeconds = writeTimeoutSeconds;
            return this;
        }

        public Builder readTimeoutSeconds(int readTimeoutSeconds) {
            this.readTimeoutSeconds = readTimeoutSeconds;
            return this;
        }

        public Builder maxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = maxIdleConnections;
            return this;
        }

        public Builder keepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
            return this;
        }

        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        public Builder retryOnConnectionFailure(AbstractRetryController retryController) {
            this.retryController = retryController;
            return this;
        }

        public Builder useConnectionPool(boolean useConnectionPool) {
            this.useConnectionPool = useConnectionPool;
            return this;
        }

        public Builder preheatHandler(BasePreheatHandler preheatHandler) {
            this.preheatHandler = preheatHandler;
            return this;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            this.networkInterceptors.add(interceptor);
            return this;
        }

        public Builder timeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public Builder monitor(boolean monitor) {
            this.monitor = monitor;
            return this;
        }

        public OkClient build() {
            ConnectionPool connectionPool = null;
            if (StringUtils.isBlank(name)) {
                name = "DEFAULT_OKHTTP_CLIENT";
            }
            final Tag tag = Tag.of("client", name);
            if (this.useConnectionPool) {
                //设置连接池，并监控
                connectionPool = new ConnectionPool(maxIdleConnections, keepAliveSeconds, timeUnit);
                final ArrayList<Tag> tags = new ArrayList<>();
                tags.add(tag);
                OkHttpConnectionPoolMetrics okHttpConnectionPoolMetrics = new OkHttpConnectionPoolMetrics(connectionPool, "okhttp.pool", tags, maxIdleConnections);
                okHttpConnectionPoolMetrics.bindTo(Metrics.globalRegistry);
            }
            final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeoutSeconds, timeUnit)
                    .writeTimeout(writeTimeoutSeconds, timeUnit)
                    .readTimeout(readTimeoutSeconds, timeUnit)
                    .retryOnConnectionFailure(true);
            if (this.monitor) {
                final JrlOkHttp3MetricsEventListener okHttpRequests = JrlOkHttp3MetricsEventListener.builder(Metrics.globalRegistry, "okHttpRequests").tag(tag).build();
                okHttpClientBuilder.eventListener(okHttpRequests);
            }
            if (null != connectionPool) {
                okHttpClientBuilder.connectionPool(connectionPool);
            }
            if (CollectionUtils.isNotEmpty(interceptors)) {
                interceptors.forEach(okHttpClientBuilder::addInterceptor);
            }
            if (CollectionUtils.isNotEmpty(networkInterceptors)) {
                networkInterceptors.forEach(okHttpClientBuilder::addNetworkInterceptor);
            }
            final OkClient okClient = new OkClient(okHttpClientBuilder.build(), retryController);
            if (null != preheatHandler) {
                try {
                    preheatHandler.doPreheat(okClient);
                } catch (Exception e) {
                    LOGGER.warn("OkhttpClientBuilder doPreheat error !", e);
                }
            }
            return okClient;
        }
    }
}
