package org.jrl.tools.request.okhttp3;

import okhttp3.*;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
* 调用实体
* @author JerryLong
*/
public class OkClient {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(OkClient.class);
    private static MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpRequest okHttpClient;
    private final AbstractRetryController retryController;

    public OkClient(OkHttpClient okHttpClient, AbstractRetryController retryController) {
        this.okHttpClient = new OkHttpRequest(okHttpClient);
        this.retryController = null == retryController ? new AbstractRetryController.DefaultRetryController(1) : retryController;
    }

    public Response get(String uri) throws IOException {
        return retryController.retry(() -> okHttpClient.get(uri));
    }

    public <T> T get(String uri, Class<T> clazz) throws IOException {
        return retryController.retry(() -> okHttpClient.get(uri, clazz));
    }

    public <T> T get(String uri, Map<String, String> headers, Class<T> clazz) throws IOException {
        return retryController.retry(() -> okHttpClient.get(uri, headers, clazz));
    }

    /**
     * 发送同步post请求
     *
     * @param uri
     * @param data
     */
    public Response send(String uri, String data) throws IOException {
        return retryController.retry(() -> okHttpClient.send(uri, data));
    }

    /**
     * 发起post请求
     *
     * @param uri
     * @param headers
     * @param data
     * @return
     * @throws IOException
     */
    public Response send(String uri, Map<String, String> headers, String data) throws IOException {
        return retryController.retry(() -> okHttpClient.send(uri, headers, data));
    }

    /**
     * 发起post请求-form表单
     *
     * @param uri
     * @param headers
     * @param formData
     * @return
     * @throws IOException
     */
    public Response sendForm(String uri, Map<String, String> headers, Map<String, String> formData) throws IOException {
        return retryController.retry(() -> okHttpClient.sendForm(uri, headers, formData));
    }

    /**
     * 发起post请求
     *
     * @param uri
     * @param data
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> T send(String uri, String data, Class<T> clazz) throws IOException {
        return retryController.retry(() -> okHttpClient.send(uri, data, clazz));
    }

    /**
     * 发起post请求
     *
     * @param uri
     * @param headers
     * @param data
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> T send(String uri, Map<String, String> headers, String data, Class<T> clazz) throws IOException {
        return retryController.retry(() -> okHttpClient.send(uri, headers, data, clazz));
    }

    /**
     * 发送post请求，异步有返回
     *
     * @param uri
     * @param data
     * @return
     */
    public CompletableFuture<Response> sendAsync(String uri, String data) {
        return okHttpClient.sendAsync(uri, data);
    }

    /**
     * 发送post请求，异步有返回
     *
     * @param uri
     * @param data
     * @return
     */
    public <T> CompletableFuture<T> sendAsync(String uri, String data, Class<T> clazz) {
        return okHttpClient.sendAsync(uri, null, data, clazz);
    }

    public <T> CompletableFuture<T> sendAsync(String uri, Map<String, String> headers, String data, Class<T> clazz) {
        return okHttpClient.sendAsync(uri, headers, data, clazz);
    }

    public void sendAsyncNoResponse(String uri, String data) {
        okHttpClient.sendAsyncNoResponse(uri, data);
    }

    public void close() {
        okHttpClient.okHttpClient.connectionPool().evictAll();
    }

    /**
     * 内部调用类
     */
    private static class OkHttpRequest {

        private final OkHttpClient okHttpClient;

        private OkHttpRequest(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
        }

        public Response get(String uri) throws IOException {
            return okHttpClient.newCall(new Request.Builder().url(uri).get().build()).execute();
        }


        public <T> T get(String uri, Class<T> clazz) throws IOException {
            final Response response = okHttpClient.newCall(new Request.Builder().url(uri).get().build()).execute();
            if (response.isSuccessful()) {
                return resToObject(response, clazz);
            } else {
                LOGGER.error("OkHttpClientUtils get fail ! uri : {} response code : {}, message : {}", uri, response.code(), response.message());
                throw new RuntimeException("uri : " + uri + " request fail ! message : " + response.message());
            }
        }

        public <T> T get(String uri, Map<String, String> headers, Class<T> clazz) throws IOException {
            final Request.Builder builder = new Request.Builder().url(uri).get();
            if (null != headers && headers.size() > 0) {
                headers.forEach(builder::addHeader);
            }
            final Response response = okHttpClient.newCall(builder.build()).execute();
            if (response.isSuccessful()) {
                return resToObject(response, clazz);
            } else {
                LOGGER.error("OkHttpClientUtils get fail ! uri : {} response code : {}, message : {}", uri, response.code(), response.message());
                throw new RuntimeException("uri : " + uri + " request fail ! message : " + response.message());
            }
        }

        /**
         * 发送同步post请求
         *
         * @param uri
         * @param data
         */
        public Response send(String uri, String data) throws IOException {
            return buildPostCall(uri, data).execute();
        }

        /**
         * 发起post请求
         *
         * @param uri
         * @param headers
         * @param data
         * @return
         * @throws IOException
         */
        public Response send(String uri, Map<String, String> headers, String data) throws IOException {
            return buildPostCall(uri, data, headers).execute();
        }

        /**
         * 发起post请求-form表单
         *
         * @param uri
         * @param headers
         * @param formData
         * @return
         * @throws IOException
         */
        public Response sendForm(String uri, Map<String, String> headers, Map<String, String> formData) throws IOException {
            return buildPostFormCall(uri, headers, formData).execute();
        }

        /**
         * 发起post请求
         *
         * @param uri
         * @param data
         * @param clazz
         * @param <T>
         * @return
         * @throws IOException
         */
        public <T> T send(String uri, String data, Class<T> clazz) throws IOException {
            Response response = buildPostCall(uri, data).execute();
            if (response.isSuccessful()) {
                return resToObject(response, clazz);
            } else {
                LOGGER.error("OkHttpClientUtils send fail ! uri : {} response code : {}, message : {}", uri, response.code(), response.message());
                throw new RuntimeException("uri : " + uri + " request fail ! message : " + response.message());
            }
        }

        /**
         * 发起post请求
         *
         * @param uri
         * @param headers
         * @param data
         * @param clazz
         * @param <T>
         * @return
         * @throws IOException
         */
        public <T> T send(String uri, Map<String, String> headers, String data, Class<T> clazz) throws IOException {
            final Response response = buildPostCall(uri, data, headers).execute();
            if (response.isSuccessful()) {
                return resToObject(response, clazz);
            } else {
                LOGGER.error("OkHttpClientUtils send fail ! uri : {} response code : {}, message : {}", uri, response.code(), response.message());
                throw new RuntimeException("uri : " + uri + " request fail ! message : " + response.message());
            }
        }

        /**
         * 发送post请求，异步有返回
         *
         * @param uri
         * @param data
         * @return
         */
        public CompletableFuture<Response> sendAsync(String uri, String data) {
            Call call = buildPostCall(uri, data);
            CompletableFuture<Response> future = new CompletableFuture<>();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LOGGER.warn("OkHttp3Client send error !", e);
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    future.complete(response);
                }
            });
            return future;
        }

        /**
         * 发送post请求，异步有返回
         *
         * @param uri
         * @param headers
         * @param data
         * @return
         */
        public <T> CompletableFuture<T> sendAsync(String uri, Map<String, String> headers, String data, Class<T> clazz) {
            Call call = buildPostCall(uri, data, headers);
            CompletableFuture<T> future = new CompletableFuture<>();
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LOGGER.warn("OkHttp3Client send error !", e);
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            future.complete(resToObject(response, clazz));
                        } catch (Throwable e) {
                            future.completeExceptionally(new RuntimeException("uri : " + uri + " request fail ! complete error ! message : " + e.getMessage()));
                        }
                    } else {
                        future.completeExceptionally(new RuntimeException("uri : " + uri + " request fail ! message : " + response.message()));
                    }
                }
            });
            return future;
        }

        /**
         * 发送post请求，异步无返回
         *
         * @param uri
         * @param data
         */
        public void sendAsyncNoResponse(String uri, String data) {
            Call call = buildPostCall(uri, data);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LOGGER.warn("OkHttp3Client send error !", e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful()) {
                        LOGGER.warn("OkHttpUtils sendAsyncNoResponse fail ! response : {}", response.toString());
                    }
                    response.close();
                }
            });
        }

        /**
         * 构建Call对象
         *
         * @param uri
         * @param data
         * @return
         */
        private Call buildPostCall(String uri, String data) {
            return okHttpClient.newCall(new Request.Builder().url(uri).post(RequestBody.create(mediaType, null == data ? "" : data)).build());
        }

        private Call buildPostCall(String uri, String data, Map<String, String> headers) {
            if (null == headers || headers.size() == 0) {
                return buildPostCall(uri, data);
            }
            final Request.Builder builder = new Request.Builder().url(uri).post(RequestBody.create(mediaType, null == data ? "" : data));
            headers.forEach(builder::addHeader);
            return okHttpClient.newCall(builder.build());
        }

        private Call buildPostFormCall(String uri, Map<String, String> headers, Map<String, String> formData) {
            final FormBody.Builder formBody = new FormBody.Builder();
            if (null != formData && formData.size() > 0) {
                formData.forEach(formBody::add);
            }
            final Request.Builder builder = new Request.Builder().url(uri).post(formBody.build());
            if (null != headers && headers.size() > 0) {
                headers.forEach(builder::addHeader);
            }
            return okHttpClient.newCall(builder.build());
        }


        private static <T> T resToObject(Response response, Class<T> clazz) throws IOException {
            if (null == response.body() || response.body().contentLength() == 0) {
                return null;
            }
            if (clazz == String.class) {
                return (T) response.body().string();
            }
            return JrlJsonNoExpUtil.fromJson(response.body().byteStream(), clazz);
        }
    }
}
