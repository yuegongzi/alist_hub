package org.alist.hub.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类
 */
public class DateTimeUtils {

    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DateTimeUtils.PATTERN_DATETIME);


    /**
     * 将LocalDateTime对象格式化为字符串（默认格式：yyyy-MM-dd HH:mm:ss）
     *
     * @param dateTime LocalDateTime对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 将LocalDateTime对象格式化为指定格式的字符串
     *
     * @param dateTime LocalDateTime对象
     * @param pattern  日期时间格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * 将字符串解析为LocalDateTime对象（默认格式：yyyy-MM-dd HH:mm:ss）
     *
     * @param datetimeStr 字符串形式的日期时间
     * @return 解析得到的LocalDateTime对象
     */
    public static LocalDateTime parse(String datetimeStr) {
        return LocalDateTime.parse(datetimeStr, DATETIME_FORMATTER);
    }

    /**
     * 将字符串按照指定格式解析为LocalDateTime对象
     *
     * @param datetimeStr 字符串形式的日期时间
     * @param pattern     日期时间格式
     * @return 解析得到的LocalDateTime对象
     */
    public static LocalDateTime parse(String datetimeStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(datetimeStr, formatter);
    }

}
