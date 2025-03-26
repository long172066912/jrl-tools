package org.jrl.redis.core.monitor;

/**
* @Title: Monitor
* @Description: 监控接口
* @author JerryLong
* @date 2021/7/1 1:59 PM
* @version V1.0
*/
public interface Monitor {

    /**
     * 执行监控
     *
     * @param monitorData 监控实体
     * @return
     */
    Object doMonitor(MonitorFactory.MonitorData monitorData);
}
