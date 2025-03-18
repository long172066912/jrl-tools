package org.jrl.tools.request.okhttp3;

import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
* 重试策略
* @author JerryLong
*/
public abstract class AbstractRetryController {
    /**
     * 重试策略
     *
     * @param supplier
     * @return
     * @throws IOException
     */
    public abstract <T> T retry(RequestSupplier<T> supplier) throws IOException;

    public static class DefaultRetryController extends AbstractRetryController {

        private static Logger LOGGER = JrlLoggerFactory.getLogger(DefaultRetryController.class);

        private int retrySize = 1;

        public DefaultRetryController() {
        }

        public DefaultRetryController(int retrySize) {
            this.retrySize = retrySize;
        }

        @Override
        public <T> T retry(RequestSupplier<T> supplier) throws IOException {
            T response = null;
            for (int i = 0; i <= retrySize; i++) {
                try {
                    response = supplier.get();
                } catch (SocketTimeoutException e) {
                    LOGGER.warn("OkhttpClient retry , i : " + i, e);
                }
                if (null != response) {
                    return response;
                }
            }
            return response;
        }
    }

    @FunctionalInterface
    public interface RequestSupplier<T> {
        /**
         * 获取结果
         *
         * @return
         * @throws IOException
         */
        T get() throws IOException;
    }
}
