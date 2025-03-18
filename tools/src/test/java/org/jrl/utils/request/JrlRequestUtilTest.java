package org.jrl.utils.request;

import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.request.JrlRequestUtil;
import org.jrl.tools.request.impl.HttpAsyncRequestHelper;
import org.jrl.tools.request.okhttp3.OkClient;
import org.jrl.tools.request.okhttp3.OkhttpClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JrlRequestUtilTest {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlRequestUtilTest.class);

    @Test
    public void testMultiAsync() throws IOException {
        OkClient okClient = OkhttpClientBuilder.builder().name("DEFAULT_CLIENT").build();
        final long l = System.currentTimeMillis();
        //模拟业务处理多次调用返回值
        Map<String, Object> data = new HashMap<>(4);
        JrlRequestUtil.multiAsync(100)
                .http(Arrays.asList(
                        HttpAsyncRequestHelper.<List>builder()
                                .okClient(okClient)
                                .task(HttpAsyncRequestHelper.HttpRequestHelper.<List>builder()
                                        .host("192.168.16.82")
                                        .port(9090)
                                        .path("/echo/testList")
                                        .params(Arrays.asList(1L, 2L))
                                        .returnType(List.class)
                                        .build())
                                .handler((s) -> data.put("testList", JrlJsonNoExpUtil.toJson(s)))
                                .build(),
                        HttpAsyncRequestHelper.<TestInfo>builder()
                                .okClient(okClient)
                                .task(HttpAsyncRequestHelper.HttpRequestHelper.<TestInfo>builder()
                                        .host("192.168.16.82")
                                        .port(9090)
                                        .path("/echo/testList")
                                        .params(Arrays.asList(1L, 2L))
                                        .returnType(TestInfo.class)
                                        .build())
                                .handler((s) -> data.put("testList1", JrlJsonNoExpUtil.toJson(s)))
                                .errorHandler(err -> data.put("testList1", JrlJsonNoExpUtil.toJson(err.getMessage())))
                                .build()
                ))
                .request();

        Assertions.assertEquals("test", data.get("testString"));
        Assertions.assertEquals(10, data.get("testInt"));
        Assertions.assertEquals("[1,2]", data.get("testList"));
        Assertions.assertNotNull(data.get("testList1"));
        final long l1 = System.currentTimeMillis() - l;
        Assertions.assertTrue(150 > l1, "超时" + l1);
    }
}