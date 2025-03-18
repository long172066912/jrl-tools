package org.jrl.tools.request.okhttp3;

import java.io.IOException;

/**
* 预热处理接口
* @author JerryLong
*/
@FunctionalInterface
public interface BasePreheatHandler {
    /**
     * 执行预热
     *
     * @param okClient
     * @throws IOException
     */
    void doPreheat(OkClient okClient) throws IOException;
}
