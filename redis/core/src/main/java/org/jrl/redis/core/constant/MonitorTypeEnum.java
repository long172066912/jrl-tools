package org.jrl.redis.core.constant;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: ConnectTypeEnum
 * @Description: 监控方式枚举
 * @date 2021/1/20 5:43 PM
 */
public enum MonitorTypeEnum {
    /**
     * 时间类
     */
    TIMER(1),
    /**
     * 统计类
     */
    COUNT(2),
    /**
     * 热key
     */
    HOTKEY(3),
    /**
     * 缓存命中率
     */
    HITKEY(4),
    ;

    MonitorTypeEnum(int type) {
        this.type = type;
    }

    /**
     * 操作类型
     */
    private int type;

    public int getType() {
        return type;
    }


    public static void main(String[] args) {
        System.out.println(MonitorTypeEnum.valueOf("SIMPLE"));
    }
}
