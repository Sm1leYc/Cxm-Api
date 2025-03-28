package com.yuan.api.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 日期时间工具类
 *
 * @author
 * @date
 */
public class DateUtils {

    /* 默认的日期格式化样式（yyyy-MM-dd） */
    public static final String SIMPLE_DATE_PATTERN = "yyyy-MM-dd";
    public static final DateTimeFormatter SIMPLE_DATE_FORMATER = DateTimeFormatter.ofPattern(SIMPLE_DATE_PATTERN);
    /* 默认的时间格式化样式（HH:mm:ss） */
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);
    /* 默认的日期时间格式化样式（yyyy-MM-dd HH:mm:ss） */
    public static final String FULL_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter FULL_DATE_FORMATER = DateTimeFormatter.ofPattern(FULL_DATE_PATTERN);


    /**
     * String(timestamp) -> String(yyyy-MM-dd HH:mm:ss)
     *
     * @param timestamp 时间戳
     *
     * @return 日期时间
     */
    public static String stamp2DateTime(String timestamp) {
        if (StringUtils.isEmpty(timestamp)) {
            return null;
        }
        String dateTime;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FULL_DATE_PATTERN);
        long lt = new Long(timestamp);
        Date date = new Date(lt);
        dateTime = simpleDateFormat.format(date);
        return dateTime;
    }

    /**
     * String(yyyy-MM-dd HH:mm:ss) -> String(timestamp)
     *
     * @param dateTime 日期时间
     *
     * @return 时间戳
     *
     * @throws ParseException 解析异常
     */
    public static String dateTime2Stamp(String dateTime) throws ParseException {
        if (StringUtils.isEmpty(dateTime)) {
            return null;
        }
        String timestamp;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FULL_DATE_PATTERN);
        Date date = simpleDateFormat.parse(dateTime);
        long ts = date.getTime();
        timestamp = String.valueOf(ts);
        return timestamp;
    }

    public static String formatLocalDate(LocalDate date, String pattern) {
        if (StringUtils.equals(pattern, "yyyyMMdd")) {
            return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } else if (StringUtils.equals(pattern, "yyyy-MM-dd")) {
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            return null;
        }
    }

    /**
     * LocalDate -> String(yyyy-MM-dd)
     *
     * @param date localDate
     *
     * @return 组装的时间
     */
    public static String formatLocalDate(LocalDate date) {
        return date.format(SIMPLE_DATE_FORMATER);
    }

    /**
     * LocalTime -> String(HH:mm:ss)
     *
     * @param time localtime
     *
     * @return 组装的时间
     */
    public static String formatLocalDate(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    /**
     * LocalDateTime -> String(yyyy-MM-dd HH:mm:ss)
     *
     * @param localDateTime localDateTime
     *
     * @return 组装的时间
     */
    public static String formatLocalDate(LocalDateTime localDateTime) {
        return localDateTime.format(FULL_DATE_FORMATER);
    }

    /**
     * 获取当前日期和时间
     *
     * @return 返回样例：2020-04-12 19:30:29
     */
    public static String getCurrentDateAndTime() {
        return format(new Date());
    }

    /**
     * 将Date格式化成默认日期时间格式的字符串（yyyy-MM-dd HH:mm:ss）
     *
     * @param date 日期时间
     *
     * @return 返回样例：2020-04-12 19:30:29
     */
    public static String format(Date date) {
        SimpleDateFormat formatTool = new SimpleDateFormat();
        formatTool.applyPattern(FULL_DATE_PATTERN);
        return formatTool.format(date);
    }

    /**
     * 将Date格式化成默认日期格式的字符串（yyyy-MM-dd）
     *
     * @param date 日期时间
     *
     * @return 返回样例：2020-04-12
     */
    public static String formatDate(Date date) {
        SimpleDateFormat formatTool = new SimpleDateFormat();
        formatTool.applyPattern(SIMPLE_DATE_PATTERN);
        return formatTool.format(date);
    }

    /**
     * 将Date格式化成默认时间格式的字符串（HH:mm:ss）
     *
     * @param date 日期时间
     *
     * @return 返回样例：19:30:29
     */
    public static String formatTime(Date date) {
        SimpleDateFormat formatTool = new SimpleDateFormat();
        formatTool.applyPattern(TIME_PATTERN);
        return formatTool.format(date);
    }

    /**
     * 按照指定pattern格式格式化Date
     *
     * @param date    日期时间
     * @param pattern 样例：yyyy/MM/dd
     *
     * @return 返回样例：2020/04/12
     */
    public static String format(Date date, String pattern) {
        SimpleDateFormat formatTool = new SimpleDateFormat();
        formatTool.applyPattern(pattern);
        return formatTool.format(date);
    }


    /**
     * 计算LocalDateTime时间差
     *
     * @param fromTime 起始时间
     * @param toTime   结束时间
     *
     * @return 相差的毫秒值
     */
    public static Long millsBetween(LocalDateTime fromTime, LocalDateTime toTime) {
        Duration between = Duration.between(fromTime, toTime);
        return between.toMillis();
    }

    /**
     * 毫秒值转换成时分秒毫秒
     *
     * @param mills 毫秒值
     *
     * @return 时分秒毫秒
     */
    public static String millsFormat(Long mills) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = mills / dd;
        Long hour = (mills - day * dd) / hh;
        Long minute = (mills - day * dd - hour * hh) / mi;
        Long second = (mills - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = mills - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + "天");
        }
        if (hour > 0) {
            sb.append(hour + "小时");
        }
        if (minute > 0) {
            sb.append(minute + "分");
        }
        if (second > 0) {
            sb.append(second + "秒");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond + "毫秒");
        }
        return sb.toString();
    }

    /**
     * 日期天数计算：包含头尾
     *
     * @param start 开始时间
     * @param end   结束时间
     *
     * @return 相差天数
     */
    public static long dateCount(Date start, Date end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        long days = (end.getTime() - start.getTime()) / (60 * 60 * 24 * 1000);
        return days + 1;
    }

    /**
     * 日期天数计算：包含头尾
     *
     * @param start 开始时间
     * @param end   结束时间
     *
     * @return 相差天数
     */
    public static long dateCount(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        Date startDate = Date.from(start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        return dateCount(startDate, endDate);
    }

    /**
     * 日期天数计算：不包含头尾
     *
     * @param start 开始时间
     * @param end   结束时间
     *
     * @return 相差天数
     */
    public static long dateBetween(Date start, Date end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        long days = (end.getTime() - start.getTime()) / (60 * 60 * 24 * 1000);
        return days;
    }

    /**
     * 计算两个日期之间的分钟差
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return 返回两个日期之间的分钟差
     */
    public static long minuteBetween(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return (cal2.getTimeInMillis() - cal1.getTimeInMillis()) / 60000;
    }

    /**
     * 日期天数计算：不包含头尾
     *
     * @param start 开始时间
     * @param end   结束时间
     *
     * @return 相差天数
     */
    public static long dateBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);

        Date startDate = Date.from(start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        return dateBetween(startDate, endDate);
    }

    /**
     * 计算两个日期的天数差距（忽略时分秒）
     *
     * @param start 开始时间
     * @param end   结束时间
     *
     * @return 间隔天数，如果开始时间大于结束时间，返回值为负数
     */
    public static int countDaysIn(Date start, Date end) {

        LocalDate ldStart = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ldend = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return (int) (ldend.toEpochDay() - ldStart.toEpochDay());
    }

    /**
     * 比较两个Date是否是同一天
     *
     * @param date        第一个时间
     * @param anotherDate 第二个时间
     *
     * @return 是则返回true
     */
    public static boolean isSameDay(Date date, Date anotherDate) {

        Objects.requireNonNull(date);
        Objects.requireNonNull(anotherDate);

        String dateStr = formatDate(date);
        String anotherDateStr = formatDate(anotherDate);

        return dateStr.equals(anotherDateStr);
    }

    /**
     * 比较两个Date的时间是否是同时
     *
     * @param date        第一个时间
     * @param anotherDate 第二个时间
     *
     * @return 是则返回true
     */
    public static boolean isSameTime(Date date, Date anotherDate) {

        Objects.requireNonNull(date);
        Objects.requireNonNull(anotherDate);

        return formatTime(date).equals(formatTime(anotherDate));
    }

    /**
     * 计算两个日期的天数差距
     *
     * @param start 开始时间
     * @param end   结束时间
     *
     * @return 间隔天数，如果开始时间大于结束时间，返回值为负数
     */
    public static Long count2Date(Date start, Date end) {
        Long diff = (start.getTime() - end.getTime()) / 86400000;
        return Math.abs(diff);
    }

    /**
     * 比较第一个时间是否晚于第二个时间
     *
     * @param date        第一个时间
     * @param anotherDate 第二个时间
     *
     * @return 是则返回true
     */
    public static boolean isTimeAfter(Date date, Date anotherDate) {

        Objects.requireNonNull(date);
        Objects.requireNonNull(anotherDate);

        return formatTime(date).compareTo(formatTime(anotherDate)) > 0;
    }

    /**
     * 比较第一个日期是否晚于第二个日期
     *
     * @param date        第一个日期
     * @param anotherDate 第二个日期
     *
     * @return 是则返回true
     */
    public static Boolean isDateAfter(Date date, Date anotherDate) {

        String dateStr = format(date, SIMPLE_DATE_PATTERN);
        String anotherDateStr = format(anotherDate, SIMPLE_DATE_PATTERN);

        return dateStr.compareTo(anotherDateStr) > 0;
    }

    /**
     * Date -> LocalDate
     *
     * @param date 日期
     *
     * @return localDate
     */
    public static LocalDate transDate2LocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * LocalDate -> Date
     *
     * @param localDate 日期
     *
     * @return date
     */
    public static Date transLocalDate2Date(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date -> LocalDateTime
     *
     * @param date 日期
     *
     * @return localDateTime
     */
    public static LocalDateTime transDate2LocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDateTime -> Date
     *
     * @param localDateTime 日期
     *
     * @return date
     */
    public static Date transLocalDateTime2Date(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date -> LocalTime
     *
     * @param date 日期
     *
     * @return localTime
     */
    public static LocalTime transDate2LocalTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalTime();
    }


    public static List<LocalDate> findEveryDay(String beginTime, String endTime) {

        List<LocalDate> dateList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date begin = simpleDateFormat.parse(beginTime);
            Date end = simpleDateFormat.parse(endTime);

            dateList.add(transDate2LocalDate(begin));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(begin);
            while (end.after(calendar.getTime())) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                dateList.add(transDate2LocalDate(calendar.getTime()));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateList;
    }


    public static List<LocalDateTime> findEveryHour(LocalDate date, Integer days) {

        LocalDate endDate = date.plusDays(days);
        List<LocalDateTime> hourList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date begin = transLocalDate2Date(date);
        Date end = transLocalDate2Date(endDate);

        hourList.add(date.atStartOfDay());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        while (end.after(calendar.getTime())) {
            calendar.add(Calendar.HOUR_OF_DAY, 4);
            if (end.after(calendar.getTime())) {
                hourList.add(transDate2LocalDateTime(calendar.getTime()));
            }
        }
        return hourList;
    }

    public static List<LocalDateTime> findEveryQuarterHour(LocalDate date, Integer days) {

        LocalDate endDate = date.plusDays(days);
        List<LocalDateTime> hourList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date begin = transLocalDate2Date(date);
        Date end = transLocalDate2Date(endDate);

        hourList.add(date.atStartOfDay());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        while (end.after(calendar.getTime())) {
            calendar.add(Calendar.MINUTE, 15);
            if (end.after(calendar.getTime())) {
                hourList.add(transDate2LocalDateTime(calendar.getTime()));
            }
        }
        return hourList;
    }

    public static List<LocalDateTime> findEveryTenMinutes(LocalDateTime startHour, LocalDateTime endHour) {

        List<LocalDateTime> tenMinutesList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date begin = transLocalDateTime2Date(startHour);
        Date end = transLocalDateTime2Date(endHour);

        tenMinutesList.add(startHour);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        while (end.after(calendar.getTime())) {
            calendar.add(Calendar.MINUTE, 10);
            if (end.after(calendar.getTime())) {
                tenMinutesList.add(transDate2LocalDateTime(calendar.getTime()));
            }
        }
        return tenMinutesList;
    }

    public static LocalDateTime getNextHour(LocalDateTime localDateTime) {

        Date begin = transLocalDateTime2Date(localDateTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        calendar.add(Calendar.HOUR_OF_DAY, 4);
        LocalDateTime nextHour = transDate2LocalDateTime(calendar.getTime());
        return nextHour;
    }

    public static LocalDateTime getHalfHour(LocalDateTime localDateTime) {

        Date begin = transLocalDateTime2Date(localDateTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        calendar.add(Calendar.MINUTE, 30);
        LocalDateTime nextHour = transDate2LocalDateTime(calendar.getTime());
        return nextHour;
    }

    public static LocalDateTime getNextQuarterHour(LocalDateTime localDateTime) {

        Date begin = transLocalDateTime2Date(localDateTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        calendar.add(Calendar.MINUTE, 15);
        LocalDateTime nextHour = transDate2LocalDateTime(calendar.getTime());
        return nextHour;
    }

    public static LocalDateTime getNextTenMinutes(LocalDateTime localDateTime) {

        Date begin = transLocalDateTime2Date(localDateTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        calendar.add(Calendar.MINUTE, 10);
        LocalDateTime nextHour = transDate2LocalDateTime(calendar.getTime());
        return nextHour;
    }

}


