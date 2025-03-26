package org.jrl.redis.extend.handle.monitor.hotkey.util;

import org.jrl.redis.core.constant.CommandsDataTypeEnum;
import org.jrl.redis.executor.CacheExecutorFactory;
import org.jrl.redis.extend.handle.monitor.hotkey.model.HotKeyItem;
import org.jrl.redis.extend.handle.monitor.hotkey.model.HotKeyWriterModel;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: HotKeyLogUtils
 * @Description: 热key文件操作帮助类
 * @date 2021/7/7 2:31 PM
 */
public class HotKeyLogUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotKeyLogUtils.class);

    private static final String SERVER_NAME_END_WITH = "-";

    /**
     * 记录hotkeys到文件
     *
     * @param cacheType
     * @param hotkeys
     */
    public static void apendHotkeys(String cacheType, List<HotKeyItem> hotkeys) {
        hotkeys.stream().forEach(e -> apendFile(JrlJsonNoExpUtil.toJson(new HotKeyWriterModel(CacheExecutorFactory.getDefaultHost(cacheType), getServerName(), cacheType, CommandsDataTypeEnum.getCommandsDataType(e.getCommands()).name(), e.getKey(), e.getCount().intValue(), getNowTime()))));
    }

    private static void apendFile(String content) {
        LOGGER.debug(content);
    }

    private static String getNowTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String getServerName() {
        //todo
        return "jrl-redis";
    }
}
