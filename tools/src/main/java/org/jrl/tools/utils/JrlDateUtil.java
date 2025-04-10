package org.jrl.tools.utils;

import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具
 *
 * @author JerryLong
 */
public class JrlDateUtil {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlDateUtil.class);

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static long date2TimeStamp(String dateStr) {
        return date2TimeStamp(dateStr, DATE_FORMAT);
    }

    /**
     * 日期转时间戳
     *
     * @param dateStr 日期字符串
     * @param format  日期格式
     * @return 时间戳
     */
    public static long date2TimeStamp(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr).getTime();
        } catch (Exception e) {
            LOGGER.error("zeus-limit date2TimeStamp error ! dateStr : {} , format : {}", dateStr, format, e);
            throw new RuntimeException("zeus-limit date2TimeStamp error ! dateStr : " + dateStr + " , format : " + format);
        }
    }

    public static String timeStamp2DateStr(long timestamp) {
        return timeStamp2DateStr(timestamp, DATE_FORMAT);
    }

    /**
     * 时间戳转日期字符串
     *
     * @param timestamp 时间戳
     * @param format    日期格式
     * @return 日期字符串
     */
    public static String timeStamp2DateStr(long timestamp, String format) {
        //设置格式
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        //获得带格式的字符串
        return dateFormat.format(timestamp);
    }

    public static Date timeStamp2Date(long timestamp) {
        return timeStamp2Date(timestamp, DATE_FORMAT);
    }

    /**
     * 时间戳转日期
     *
     * @param timestamp 时间戳
     * @param format    日期格式
     * @return 日期
     */
    public static Date timeStamp2Date(long timestamp, String format) {
        //设置格式
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        //获得带格式的字符串
        String date = dateFormat.format(timestamp);
        try {
            return dateFormat.parse(date);
        } catch (Exception e) {
            LOGGER.error("zeus-limit timeStamp2Date error ! timestamp : {} , format : {}", timestamp, format, e);
            throw new RuntimeException("zeus-limit timeStamp2Date error ! timestamp : " + timestamp + " , format : " + format);
        }
    }
}
