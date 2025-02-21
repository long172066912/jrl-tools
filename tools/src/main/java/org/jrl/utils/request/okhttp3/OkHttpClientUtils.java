package org.jrl.utils.request.okhttp3;

import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * OKHttp3客户端封装，支持POST请求的同步异步，自动监控请求与连接池
 *
 * @author JerryLong
 */
public class OkHttpClientUtils {

    private static OkClient DEFAULT_CLIENT;

    /**
     * 获取默认client
     *
     * @return
     */
    public static OkClient getDefaultClient() {
        if (null == DEFAULT_CLIENT) {
            synchronized (OkHttpClientUtils.class) {
                if (null == DEFAULT_CLIENT) {
                    DEFAULT_CLIENT = OkhttpClientBuilder.builder().name("DEFAULT_CLIENT").build();
                }
            }
        }
        return DEFAULT_CLIENT;
    }

    /**
     * 发送GET请求
     *
     * @param uri
     * @return
     * @throws IOException
     */
    public static Response get(String uri) throws IOException {
        return getDefaultClient().get(uri);
    }

    public static <T> T get(String uri, Class<T> clazz) throws IOException {
        return getDefaultClient().get(uri, clazz);
    }

    public static <T> T get(String uri, Map<String, String> headers, Class<T> clazz) throws IOException {
        return getDefaultClient().get(uri, headers, clazz);
    }

    /**
     * 发送同步post请求
     *
     * @param uri
     * @param data
     */
    public static Response send(String uri, String data) throws IOException {
        return getDefaultClient().send(uri, data);
    }

    public static Response send(String uri, Map<String, String> headers, String data) throws IOException {
        return getDefaultClient().send(uri, headers, data);
    }

    /**
     * 发送同步form表单请求
     *
     * @param uri
     * @param headers
     * @param formData
     * @return
     * @throws IOException
     */
    public static Response sendForm(String uri, Map<String, String> headers, Map<String, String> formData) throws IOException {
        return getDefaultClient().sendForm(uri, headers, formData);
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
    public static <T> T send(String uri, String data, Class<T> clazz) throws IOException {
        return getDefaultClient().send(uri, data, clazz);
    }

    public static <T> T send(String uri, Map<String, String> headers, String data, Class<T> clazz) throws IOException {
        return getDefaultClient().send(uri, headers, data, clazz);
    }

    /**
     * 发送post请求，异步有返回
     *
     * @param uri
     * @param data
     * @return
     */
    public static CompletableFuture<Response> sendAsync(String uri, String data) {
        return getDefaultClient().sendAsync(uri, data);
    }

    /**
     * 发送post请求，异步有返回
     *
     * @param uri
     * @param data
     * @return
     */
    public static <T> CompletableFuture<T> sendAsync(String uri, String data, Class<T> clazz) {
        return getDefaultClient().sendAsync(uri, data, clazz);
    }

    /**
     * 发送post请求，异步无返回
     *
     * @param uri
     * @param data
     */
    public static void sendAsyncNoResponse(String uri, String data) {
        getDefaultClient().sendAsyncNoResponse(uri, data);
    }
}