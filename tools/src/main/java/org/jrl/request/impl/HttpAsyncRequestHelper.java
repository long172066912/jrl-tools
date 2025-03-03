package org.jrl.request.impl;

import org.jrl.json.JrlJsonNoExpUtil;
import org.jrl.request.okhttp3.OkHttpClientUtils;
import org.jrl.utils.JrlClassUtil;
import org.jrl.log.JrlLoggerFactory;
import org.jrl.request.JrlAsyncRequest;
import org.jrl.request.okhttp3.OkClient;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 异步控制单元
 *
 * @author JerryLong
 */
public class HttpAsyncRequestHelper<V> implements JrlAsyncRequest<V> {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(HttpAsyncRequestHelper.class);

    private final OkClient okClient;
    /**
     * 异步执行的具体逻辑
     */
    private final HttpRequestHelper<V> task;
    /**
     * 对于异步返回的结果进行加工
     */
    private final Consumer<V> handler;
    /**
     * 异常处理
     */
    private final Consumer<Throwable> errorHandler;
    private boolean isCall = false;
    private boolean isDone = false;

    protected HttpAsyncRequestHelper(OkClient okClient, HttpRequestHelper<V> future, Consumer<V> handler, Consumer<Throwable> errorHandler) {
        this.okClient = okClient;
        this.task = future;
        this.handler = handler;
        this.errorHandler = errorHandler;
    }

    public Consumer<V> getHandler() {
        return handler;
    }

    /**
     * 执行异步回调逻辑
     */
    @Override
    public void call(Runnable runnable) {
        if (isCall || isDone) {
            return;
        }
        try {
            final long l = System.currentTimeMillis();
            //发起http 异步调用
            final CompletableFuture<V> future = this.okClient.sendAsync(this.task.createUrl(), this.task.getHeaders(), this.task.getRequestParams(), this.task.getReturnType());
            if (null == future) {
                return;
            }
            future.thenAccept((v) -> {
                if (isDone) {
                    return;
                }
                isDone = true;
                try {
                    handler.accept(v);
                } catch (Throwable e) {
                    errorHandler(l, e);
                }
                runnable.run();
            }).exceptionally((e) -> {
                errorHandler(l, e);
                runnable.run();
                return null;
            });
        } catch (Throwable e) {
            LOGGER.error("jrl-rpc future-handler error !", e);
        } finally {
            isCall = true;
        }
    }

    private void errorHandler(long l, Throwable e) {
        if (null != errorHandler) {
            errorHandler.accept(e);
        } else {
            LOGGER.error("jrl-flowControl future-handler error ! cost : {}", System.currentTimeMillis() - l, e);
        }
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public void end() {
        isDone = true;
    }

    public static <V> Builder<V> builder() {
        return new Builder<>();
    }

    public static class Builder<V> {
        private OkClient okClient;
        private HttpRequestHelper<V> task;
        private Consumer<V> handler;
        private Consumer<Throwable> errorHandler;

        public Builder<V> task(HttpRequestHelper<V> future) {
            this.task = future;
            return this;
        }

        public Builder<V> handler(Consumer<V> handler) {
            this.handler = handler;
            return this;
        }

        public Builder<V> errorHandler(Consumer<Throwable> exceptionConsumer) {
            this.errorHandler = exceptionConsumer;
            return this;
        }

        public Builder<V> okClient(OkClient okClient) {
            this.okClient = okClient;
            return this;
        }

        public HttpAsyncRequestHelper<V> build() {
            if (null == okClient) {
                okClient = OkHttpClientUtils.getDefaultClient();
            }
            return new HttpAsyncRequestHelper<>(okClient, task, handler, errorHandler);
        }
    }

    public static class HttpRequestHelper<V> {
        private final String scheme;
        private final String host;
        private final int port;
        private final String path;
        private final Map<String, String> headers;
        private final Object params;
        private final Class<V> returnType;

        public HttpRequestHelper(String scheme, String host, int port, String path, Map<String, String> headers, Object params, Class<V> returnType) {
            this.scheme = scheme;
            this.host = host;
            this.port = port;
            this.path = path;
            this.headers = headers;
            this.params = params;
            this.returnType = returnType;
        }

        public String createUrl() {
            StringBuilder url = new StringBuilder();
            url.append(scheme).append("://").append(host).append(":").append(port).append(path);
            return url.toString();
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getRequestParams() {
            if (null == params) {
                return null;
            }
            if (JrlClassUtil.isWrapClass(params.getClass())) {
                return params.toString();
            }
            return JrlJsonNoExpUtil.toJson(params);
        }

        public Class<V> getReturnType() {
            return returnType;
        }

        public static <C> Builder<C> builder() {
            return new Builder<>();
        }

        public static class Builder<C> {
            private String scheme;
            private String host;
            private Integer port;
            private String path;
            private Map<String, String> headers;
            private Object params;
            private Class<C> returnType;

            public Builder<C> scheme(String scheme) {
                this.scheme = scheme;
                return this;
            }

            public Builder<C> host(String host) {
                this.host = host;
                return this;
            }

            public Builder<C> port(int port) {
                this.port = port;
                return this;
            }

            public Builder<C> path(String path) {
                this.path = path;
                return this;
            }

            public Builder<C> headers(Map<String, String> headers) {
                this.headers = headers;
                return this;
            }

            public Builder<C> params(Object params) {
                this.params = params;
                return this;
            }

            public Builder<C> returnType(Class<C> returnType) {
                this.returnType = returnType;
                return this;
            }

            public HttpRequestHelper<C> build() {
                return new HttpRequestHelper<>(
                        null == scheme ? "http" : scheme,
                        host, null == port ? 80 : port, path, headers, params, returnType);
            }
        }
    }
}