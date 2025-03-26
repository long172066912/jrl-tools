package org.jrl.monitor.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
* 监控客户端
* @author JerryLong
*/
public class JrlMonitor {
    public static ExecutorService getMonitorExecutorService(String threadPoolName, ThreadPoolExecutor poolExecutor) {
        return poolExecutor;
    }
}
