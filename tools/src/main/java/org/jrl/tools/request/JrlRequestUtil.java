package org.jrl.tools.request;

import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

/**
 * 提供调用工具类
 *
 * @author JerryLong
 */
public class JrlRequestUtil {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlRequestUtil.class);

    /**
     * 批量异步调用，纯异步处理，无额外线程处理
     *
     * @param timeout 超时时间
     */
    public static MultiAsyncRequestHelperHandler.Builder multiAsync(int timeout) {
        return MultiAsyncRequestHelperHandler.builder().timeout(timeout);
    }


}
