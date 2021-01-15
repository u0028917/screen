package com.seance.screen.common;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * Description : 日期工具类 Author : miaozh CreateTime : 2016年10月8日 下午4:38:19
 */
@Slf4j
public class DateUtils {

  private static final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<>();
  private static final Object object = new Object();

  /**
   * 获取SimpleDateFormat
   *
   * @param pattern 日期格式
   * @return SimpleDateFormat对象
   * @throws RuntimeException 异常：非法日期格式
   */
  public static SimpleDateFormat getDateFormat(String pattern)
      throws RuntimeException {
    SimpleDateFormat dateFormat = threadLocal.get();
    if (dateFormat == null) {
      synchronized (object) {
        if (dateFormat == null) {
          dateFormat = new SimpleDateFormat(pattern);
          dateFormat.setLenient(false);
          threadLocal.set(dateFormat);
        }
      }
    }
    dateFormat.applyPattern(pattern);
    return dateFormat;
  }

  /**
   * 获取日期中的某数值。如获取月份
   *
   * @param date 日期
   * @param dateType 日期格式
   * @return 数值
   */
  private static int getInteger(Date date, int dateType) {
    int num = 0;
    Calendar calendar = Calendar.getInstance();
    if (date != null) {
      calendar.setTime(date);
      num = calendar.get(dateType);
    }
    return num;
  }

  /**
   * 增加日期中某类型的某数值。如增加日期
   *
   * @param date 日期字符串
   * @param dateType 类型
   * @param amount 数值
   * @return 计算后日期字符串
   */
  private static String addInteger(String date, int dateType, int amount) {
    String dateString = null;
    DateStyle dateStyle = getDateStyle(date);
    if (dateStyle != null) {
      Date myDate = StringToDate(date, dateStyle);
      myDate = addInteger(myDate, dateType, amount);
      dateString = DateToString(myDate, dateStyle);
    }
    return dateString;
  }

  /**
   * 增加日期中某类型的某数值。如增加日期
   *
   * @param date 日期
   * @param dateType 类型
   * @param amount 数值
   * @return 计算后日期
   */
  private static Date addInteger(Date date, int dateType, int amount) {
    Date myDate = null;
    if (date != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.add(dateType, amount);
      myDate = calendar.getTime();
    }
    return myDate;
  }

  /**
   * 获取精确的日期
   *
   * @param timestamps 时间long集合
   * @return 日期
   */
  private static Date getAccurateDate(List<Long> timestamps) {
    Date date = null;
    long timestamp = 0;
    Map<Long, long[]> map = new HashMap<>();
    List<Long> absoluteValues = new ArrayList<>();

    if (!CollectionUtils.isEmpty(timestamps)) {
      if (timestamps.size() > 1) {
        for (int i = 0; i < timestamps.size(); i++) {
          for (int j = i + 1; j < timestamps.size(); j++) {
            long absoluteValue = Math.abs(timestamps.get(i)
                - timestamps.get(j));
            absoluteValues.add(absoluteValue);
            long[] timestampTmp = {timestamps.get(i),
                timestamps.get(j)};
            map.put(absoluteValue, timestampTmp);
          }
        }

        // 有可能有相等的情况。如2012-11和2012-11-01。时间戳是相等的。此时minAbsoluteValue为0
        // 因此不能将minAbsoluteValue取默认值0
        long minAbsoluteValue = -1;
        if (!absoluteValues.isEmpty()) {
          minAbsoluteValue = absoluteValues.get(0);
          for (int i = 1; i < absoluteValues.size(); i++) {
            if (minAbsoluteValue > absoluteValues.get(i)) {
              minAbsoluteValue = absoluteValues.get(i);
            }
          }
        }

        if (minAbsoluteValue != -1) {
          long[] timestampsLastTmp = map.get(minAbsoluteValue);

          long dateOne = timestampsLastTmp[0];
          long dateTwo = timestampsLastTmp[1];
          if (absoluteValues.size() > 1) {
            timestamp = Math.abs(dateOne) > Math.abs(dateTwo) ? dateOne
                : dateTwo;
          }
        }
      } else {
        timestamp = timestamps.get(0);
      }
    }

    if (timestamp != 0) {
      date = new Date(timestamp);
    }
    return date;
  }

  /**
   * 根据开始时间和结束时间获取间隔的时间（单位/小时）
   */
  public static Double getIntervalHours(String startTime, String endTime) {
    BigDecimal startTimeStamp = BigDecimal
        .valueOf(StringToDate(startTime, DateStyle.HH_MM_SS).getTime());
    BigDecimal endTimeStamp = BigDecimal
        .valueOf(StringToDate(endTime, DateStyle.HH_MM_SS).getTime());
    BigDecimal hours = endTimeStamp.subtract(startTimeStamp)
        .divide(new BigDecimal(1000 * 60 * 60), 2, BigDecimal.ROUND_DOWN);
    return hours.doubleValue();
  }

  /**
   * 判断字符串是否为日期字符串
   *
   * @param date 日期字符串
   * @return true or false
   */
  public static boolean isDate(String date) {
    boolean isDate = false;
    if (date != null) {
      if (getDateStyle(date) != null) {
        isDate = true;
      }
    }
    return isDate;
  }

  /**
   * 获取日期字符串的日期风格。失敗返回null。
   *
   * @param date 日期字符串
   * @return 日期风格
   */
  public static DateStyle getDateStyle(String date) {
    DateStyle dateStyle = null;
    Map<Long, DateStyle> map = new HashMap<>();
    List<Long> timestamps = new ArrayList<>();
    for (DateStyle style : DateStyle.values()) {
      if (style.isShowOnly()) {
        continue;
      }
      Date dateTmp = null;
      if (date != null) {
        try {
          ParsePosition pos = new ParsePosition(0);
          dateTmp = getDateFormat(style.getValue()).parse(date, pos);
          if (pos.getIndex() != date.length()) {
            dateTmp = null;
          }
        } catch (Exception e) {
          log.warn("DateUtils.getDateStyle方法异常：", e);
        }
      }
      if (dateTmp != null) {
        timestamps.add(dateTmp.getTime());
        map.put(dateTmp.getTime(), style);
      }
    }
    Date accurateDate = getAccurateDate(timestamps);
    if (accurateDate != null) {
      dateStyle = map.get(accurateDate.getTime());
    }
    return dateStyle;
  }

  /**
   * 将日期字符串转化为日期。失败返回null。
   *
   * @param date 日期字符串
   * @return 日期
   */
  public static Date StringToDate(String date) {
    DateStyle dateStyle = getDateStyle(date);
    return StringToDate(date, dateStyle);
  }

  /**
   * 将日期字符串转化为日期。失败返回null。
   *
   * @param date 日期字符串
   * @param pattern 日期格式
   * @return 日期
   */
  public static Date StringToDate(String date, String pattern) {
    Date myDate = null;
    if (date != null) {
      try {
        myDate = getDateFormat(pattern).parse(date);
      } catch (Exception e) {
        log.warn("DateUtils.StringToDate方法异常：", e);
      }
    }
    return myDate;
  }


  /**
   * 将日期字符串转化为日期。失败返回null。
   *
   * @param date 日期字符串
   * @param dateStyle 日期风格
   * @return 日期
   */
  public static Date StringToDate(String date, DateStyle dateStyle) {
    Date myDate = null;
    if (dateStyle != null) {
      myDate = StringToDate(date, dateStyle.getValue());
    }
    return myDate;
  }

  /**
   * 将日期转化为日期字符串。失败返回null。
   *
   * @param date 日期
   * @param pattern 日期格式
   * @return 日期字符串
   */
  public static String DateToString(Date date, String pattern) {
    String dateString = null;
    if (date != null) {
      try {
        dateString = getDateFormat(pattern).format(date);
      } catch (Exception e) {
        log.warn("DateUtils.DateToString方法异常：", e);
      }
    }
    return dateString;
  }

  /**
   * 将日期转化为日期字符串。失败返回null。
   *
   * @param date 日期
   * @param dateStyle 日期风格
   * @return 日期字符串
   */
  public static String DateToString(Date date, DateStyle dateStyle) {
    String dateString = null;
    if (dateStyle != null) {
      dateString = DateToString(date, dateStyle.getValue());
    }
    return dateString;
  }

  /**
   * 将日期字符串转化为另一日期字符串。失败返回null。
   *
   * @param date 旧日期字符串
   * @param newPattern 新日期格式
   * @return 新日期字符串
   */
  public static String stringToString(String date, String newPattern) {
    DateStyle oldDateStyle = getDateStyle(date);
    return stringToString(date, oldDateStyle, newPattern);
  }

  /**
   * 将日期字符串转化为另一日期字符串。失败返回null。
   *
   * @param date 旧日期字符串
   * @param newDateStyle 新日期风格
   * @return 新日期字符串
   */
  public static String stringToString(String date, DateStyle newDateStyle) {
    DateStyle oldDateStyle = getDateStyle(date);
    return stringToString(date, oldDateStyle, newDateStyle);
  }

  /**
   * 将日期字符串转化为另一日期字符串。失败返回null。
   *
   * @param date 旧日期字符串
   * @param olddPattern 旧日期格式
   * @param newPattern 新日期格式
   * @return 新日期字符串
   */
  public static String stringToString(String date, String olddPattern,
      String newPattern) {
    return DateToString(StringToDate(date, olddPattern), newPattern);
  }

  /**
   * 将日期字符串转化为另一日期字符串。失败返回null。
   *
   * @param date 旧日期字符串
   * @param olddDteStyle 旧日期风格
   * @param newParttern 新日期格式
   * @return 新日期字符串
   */
  public static String stringToString(String date, DateStyle olddDteStyle,
      String newParttern) {
    String dateString = null;
    if (olddDteStyle != null) {
      dateString = stringToString(date, olddDteStyle.getValue(),
          newParttern);
    }
    return dateString;
  }

  /**
   * 将日期字符串转化为另一日期字符串。失败返回null。
   *
   * @param date 旧日期字符串
   * @param olddPattern 旧日期格式
   * @param newDateStyle 新日期风格
   * @return 新日期字符串
   */
  public static String stringToString(String date, String olddPattern,
      DateStyle newDateStyle) {
    String dateString = null;
    if (newDateStyle != null) {
      dateString = stringToString(date, olddPattern,
          newDateStyle.getValue());
    }
    return dateString;
  }

  /**
   * 将日期字符串转化为另一日期字符串。失败返回null。
   *
   * @param date 旧日期字符串
   * @param olddDteStyle 旧日期风格
   * @param newDateStyle 新日期风格
   * @return 新日期字符串
   */
  public static String stringToString(String date, DateStyle olddDteStyle,
      DateStyle newDateStyle) {
    String dateString = null;
    if (olddDteStyle != null && newDateStyle != null) {
      dateString = stringToString(date, olddDteStyle.getValue(),
          newDateStyle.getValue());
    }
    return dateString;
  }

  /**
   * 格式化时间
   *
   * @return 时间
   */
  public static Date dateForMat(Date date, DateStyle dateStyle) {
    return StringToDate(DateToString(date, dateStyle), dateStyle);
  }

  /**
   * 增加日期的年份。失败返回null。
   *
   * @param date 日期
   * @param yearAmount 增加数量。可为负数
   * @return 增加年份后的日期字符串
   */
  public static String addYear(String date, int yearAmount) {
    return addInteger(date, Calendar.YEAR, yearAmount);
  }

  /**
   * 增加日期的值。失败返回null。
   *
   * @param date 日期
   * @param type 添加的类型 年、月、日、时、分、秒
   * @param amount 增加数量
   */
  public static Date add(Date date, int type, int amount) {
    return addInteger(date, type, amount);
  }

  /**
   * 增加日期的年份。失败返回null。
   *
   * @param date 日期
   * @param yearAmount 增加数量。可为负数
   * @return 增加年份后的日期
   */
  public static Date addYear(Date date, int yearAmount) {
    return addInteger(date, Calendar.YEAR, yearAmount);
  }

  /**
   * 增加日期的月份。失败返回null。
   *
   * @param date 日期
   * @param monthAmount 增加数量。可为负数
   * @return 增加月份后的日期字符串
   */
  public static String addMonth(String date, int monthAmount) {
    return addInteger(date, Calendar.MONTH, monthAmount);
  }

  /**
   * 增加日期的月份。失败返回null。
   *
   * @param date 日期
   * @param monthAmount 增加数量。可为负数
   * @return 增加月份后的日期
   */
  public static Date addMonth(Date date, int monthAmount) {
    return addInteger(date, Calendar.MONTH, monthAmount);
  }

  /**
   * 增加日期的天数。失败返回null。
   *
   * @param date 日期字符串
   * @param dayAmount 增加数量。可为负数
   * @return 增加天数后的日期字符串
   */
  public static String addDay(String date, int dayAmount) {
    return addInteger(date, Calendar.DATE, dayAmount);
  }

  /**
   * 增加日期的天数。失败返回null。
   *
   * @param date 日期
   * @param dayAmount 增加数量。可为负数
   * @return 增加天数后的日期
   */
  public static Date addDay(Date date, int dayAmount) {
    return addInteger(date, Calendar.DATE, dayAmount);
  }

  /**
   * 增加日期的小时。失败返回null。
   *
   * @param date 日期字符串
   * @param hourAmount 增加数量。可为负数
   * @return 增加小时后的日期字符串
   */
  public static String addHour(String date, int hourAmount) {
    return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
  }

  /**
   * 增加日期的小时。失败返回null。
   *
   * @param date 日期
   * @param hourAmount 增加数量。可为负数
   * @return 增加小时后的日期
   */
  public static Date addHour(Date date, int hourAmount) {
    return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
  }

  /**
   * 增加日期的分钟。失败返回null。
   *
   * @param date 日期字符串
   * @param minuteAmount 增加数量。可为负数
   * @return 增加分钟后的日期字符串
   */
  public static String addMinute(String date, int minuteAmount) {
    return addInteger(date, Calendar.MINUTE, minuteAmount);
  }

  /**
   * 增加日期的分钟。失败返回null。
   *
   * @param date 日期
   * @param minuteAmount 增加数量。可为负数
   * @return 增加分钟后的日期
   */
  public static Date addMinute(Date date, int minuteAmount) {
    return addInteger(date, Calendar.MINUTE, minuteAmount);
  }

  /**
   * 增加日期的秒钟。失败返回null。
   *
   * @param date 日期字符串
   * @param secondAmount 增加数量。可为负数
   * @return 增加秒钟后的日期字符串
   */
  public static String addSecond(String date, int secondAmount) {
    return addInteger(date, Calendar.SECOND, secondAmount);
  }

  /**
   * 增加日期的秒钟。失败返回null。
   *
   * @param date 日期
   * @param secondAmount 增加数量。可为负数
   * @return 增加秒钟后的日期
   */
  public static Date addSecond(Date date, int secondAmount) {
    return addInteger(date, Calendar.SECOND, secondAmount);
  }

  /**
   * 获取日期的年份。失败返回0。
   *
   * @param date 日期字符串
   * @return 年份
   */
  public static int getYear(String date) {
    return getYear(StringToDate(date));
  }

  /**
   * 获取日期的年份。失败返回0。
   *
   * @param date 日期
   * @return 年份
   */
  public static int getYear(Date date) {
    return getInteger(date, Calendar.YEAR);
  }

  /**
   * 获取日期的月份。失败返回0。
   *
   * @param date 日期字符串
   * @return 月份
   */
  public static int getMonth(String date) {
    return getMonth(StringToDate(date));
  }

  /**
   * 获取日期的月份。失败返回0。
   *
   * @param date 日期
   * @return 月份
   */
  public static int getMonth(Date date) {
    return getInteger(date, Calendar.MONTH) + 1;
  }

  /**
   * 获取日期的天数。失败返回0。
   *
   * @param date 日期字符串
   * @return 天
   */
  public static int getDay(String date) {
    return getDay(StringToDate(date));
  }

  /**
   * 获取日期的天数。失败返回0。
   *
   * @param date 日期
   * @return 天
   */
  public static int getDay(Date date) {
    return getInteger(date, Calendar.DATE);
  }

  /**
   * 获取日期的小时。失败返回0。
   *
   * @param date 日期字符串
   * @return 小时
   */
  public static int getHour(String date) {
    return getHour(StringToDate(date));
  }

  /**
   * 获取日期的小时。失败返回0。
   *
   * @param date 日期
   * @return 小时
   */
  public static int getHour(Date date) {
    return getInteger(date, Calendar.HOUR_OF_DAY);
  }

  /**
   * 获取日期的分钟。失败返回0。
   *
   * @param date 日期字符串
   * @return 分钟
   */
  public static int getMinute(String date) {
    return getMinute(StringToDate(date));
  }

  /**
   * 获取日期的分钟。失败返回0。
   *
   * @param date 日期
   * @return 分钟
   */
  public static int getMinute(Date date) {
    return getInteger(date, Calendar.MINUTE);
  }

  /**
   * 获取日期的秒钟。失败返回0。
   *
   * @param date 日期字符串
   * @return 秒钟
   */
  public static int getSecond(String date) {
    return getSecond(StringToDate(date));
  }

  /**
   * 获取日期的秒钟。失败返回0。
   *
   * @param date 日期
   * @return 秒钟
   */
  public static int getSecond(Date date) {
    return getInteger(date, Calendar.SECOND);
  }

  /**
   * 获取日期 。默认yyyy-MM-dd格式。失败返回null。
   *
   * @param date 日期字符串
   * @return 日期
   */
  public static String getDate(String date) {
    return stringToString(date, DateStyle.YYYY_MM_DD);
  }

  /**
   * 获取日期 。默认yyyy-MM-dd格式。失败返回null。
   *
   * @param date 日期字符串
   * @return 日期
   */
  public static String getDateHhSs(Date date) {
    return DateToString(date, DateStyle.YYYY_MM_DD_HH_MM_SS);
  }

  /**
   * 获取日期。默认yyyy-MM-dd格式。失败返回null。
   *
   * @param date 日期
   * @return 日期
   */
  public static String getDate(Date date) {
    return DateToString(date, DateStyle.YYYY_MM_DD);
  }

  /**
   * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
   *
   * @param date 日期字符串
   * @return 时间
   */
  public static String getTime(String date) {
    return stringToString(date, DateStyle.HH_MM_SS);
  }

  /**
   * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
   *
   * @param date 日期
   * @return 时间
   */
  public static String getTime(Date date) {
    return DateToString(date, DateStyle.HH_MM_SS);
  }



  /**
   * 获取两个日期相差的天数
   *
   * @param date 日期字符串
   * @param otherDate 另一个日期字符串
   * @return 相差天数。如果失败则返回-1
   */
  public static int getIntervalDays(String date, String otherDate) {
    return getIntervalDays(StringToDate(date), StringToDate(otherDate));
  }

  /**
   * @param date 日期
   * @param otherDate 另一个日期
   * @return 相差天数。如果失败则返回-1
   */
  public static int getIntervalDays(Date date, Date otherDate) {
    int num = -1;
    Date dateTmp = StringToDate(DateUtils.getDate(date),
        DateStyle.YYYY_MM_DD);
    Date otherDateTmp = StringToDate(DateUtils.getDate(otherDate),
        DateStyle.YYYY_MM_DD);
    if (dateTmp != null && otherDateTmp != null) {
      long time = Math.abs(dateTmp.getTime() - otherDateTmp.getTime());
      num = (int) (time / (24 * 60 * 60 * 1000));
    }
    return num;
  }

  /**
   * @param date 日期
   * @param otherDate 另一个日期
   * @return 相差分钟数。如果失败则返回-1
   */
  public static int getIntervalMinute(Date date, Date otherDate) {
    int num = -1;
    if (date != null && otherDate != null) {
      long time = Math.abs(date.getTime() - otherDate.getTime());
      num = (int) (time / (1000 * 60));
    }
    return num;
  }


  /**
   * Description :获取当前时间yyyyMMddHHmmss字符串 Author : miaozh CreateTime : 2016年10月10日 下午2:43:39
   *
   * @return String
   */
  public static String getSecondString() {
    return DateToString(new Date(), DateStyle.YYYYMMDDHHMMSS);
  }

  /**
   * Description : 获取当前时间yyyyMMddHHmmssSSS字符串 Author : miaozh CreateTime : 2016年10月10日 下午2:46:08
   *
   * @return String
   */
  public static String getMillisecondString() {
    return DateToString(new Date(), DateStyle.YYYYMMDDHHMMSSSSS);
  }





  /**
   * Discription:[判断两个日期是否相等]
   *
   * @param d1 日期1
   * @param d2 日期2
   * @return Created on 2017/11/3
   * @author: 尹归晋
   */
  public static boolean isSameDate(Date d1, Date d2) {
    if (null == d1 || null == d2) {
      return false;
    }
    //return getOnlyDate(d1).equals(getOnlyDate(d2));
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(d1);
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(d2);
    return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(
        Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
  }

  /**
   * Discription:[获取两个时间的时间差]
   *
   * @param date1 时间一
   * @param date2 时间二
   * @param type 获取的类型  天 时 分
   * @return 时间一 减 时间二 的值
   *
   * Created on 2017/11/10
   * @author: 尹归晋
   */
  public static int dayDiff(Date date1, Date date2, int type) {
    long diff = date1.getTime() - date2.getTime();
    long nd = 1000 * 24 * 60 * 60L;
    long nh = 1000 * 60 * 60L;
    long nm = 1000 * 60L;
    // 计算差多少天
    long day = diff / nd;
    // 计算差多少小时
    long hour = diff / nh;
    // 计算差多少分钟
    long min = diff / nm;
    if (Calendar.DATE == type) {
      return (int) day;
    } else if (Calendar.HOUR_OF_DAY == type) {
      return (int) hour;
    } else {
      return (int) min;
    }
  }



  /**
   * <p>Description:[计算相差的月份，占用一个整月（比如2-1至2-28）按一个月，但是非整月部分按和15、31比较算：
   * 如2-01到2-28是1个月 2-01到3-01是1.5个月 2-02到3-01是1.0个月 ]</p> Created on 2017年11月28日
   *
   * @param startDate 开始时间
   * @param endDate 结束时间
   * @return 相差的月数0.5的倍数
   * @author 缪志红
   */
  public static double getMonths(Date startDate, Date endDate) {
    int startYear = DateUtils.getYear(startDate);
    int endYear = DateUtils.getYear(endDate);
    int startMonth = DateUtils.getMonth(startDate);
    int endMonth = DateUtils.getMonth(endDate);
    int startDay = DateUtils.getDay(startDate);
    int endDay = DateUtils.getDay(endDate);
    int endLastDay = DateUtils.getLastDay(endDate);
    int startLastDay = DateUtils.getLastDay(startDate);
    int intervalYears = endYear - startYear;
    double intervalMonths;
    int intervalDays;
    if (intervalYears >= 1) {
      intervalDays = (startLastDay - startDay) + endDay + 1;
      intervalMonths = (12.0 - startMonth) + endMonth + 1;
      if (startDay == 1 && endDay == endLastDay) {
        intervalDays = 0;
      } else {
        if (startDay != 1) {
          intervalMonths -= 1.0;
        } else {
          intervalDays = endDay;
        }
        if (endDay != endLastDay) {
          intervalMonths -= 1.0;
        } else {
          intervalDays = startLastDay - startDay + 1;
        }
      }
    } else {
      intervalMonths = endMonth - startMonth + 1.0;
      if (intervalMonths > 1.0) {
        intervalDays = (startLastDay - startDay) + endDay + 1;
        if (startDay == 1 && endDay == endLastDay) {
          intervalDays = 0;
        } else {
          if (startDay != 1) {
            intervalMonths -= 1.0;
          } else {
            intervalDays = endDay;
          }
          if (endDay != endLastDay) {
            intervalMonths -= 1.0;
          } else {
            intervalDays = startLastDay - startDay + 1;
          }
        }
        intervalMonths = intervalMonths < 0.0 ? 0.0 : intervalMonths;
      } else {
        intervalMonths = 0.0;
        intervalDays = endDay - startDay + 1;
      }

    }
    if (intervalDays == 0) {
      intervalMonths += 0.0;
    } else if (intervalDays <= 15) {
      intervalMonths += 0.5;
    } else if (intervalDays >= 31) {
      intervalMonths += 1.0;
      if (intervalDays - 31 > 15) {
        intervalMonths += 1.0;
      } else if (intervalDays - 31 >= 1) {
        intervalMonths += 0.5;
      }
    } else {
      intervalMonths += 1.0;
    }
    for (int i = 1; i < intervalYears; i++) {
      intervalMonths += 12.0;
    }
    return intervalMonths;
  }


  /**
   * <p>Description:[计算当前时间所处的拆分时间段]</p>
   * Created on 2017年12月05日
   *
   * @return 拆分时间段Integer值
   * @author 缪志红
   */
  public static Integer getSplitTimeByNow() {
    return getSplitTime(new Date());
  }

  /**
   * <p>Description:[计算时间所处的拆分时间段]</p>
   * Created on 2018年1月14日
   *
   * @return 拆分时间段Integer值
   * @author 缪志红
   */
  public static Integer getSplitTime(Date date) {
    int hour = DateUtils.getHour(date);
    int minute = DateUtils.getMinute(date);
    int splitTime = hour * 2 + 1;
    if (minute > 30) {
      splitTime += 1;
    }
    return splitTime;
  }

  /**
   * Discription:[获取当前月的第一天的日期]
   *
   * @return 当前月第一天
   *
   * Created on 2017/12/15
   * @author: 尹归晋
   */
  public static Date getfirstDate() {
    Calendar c = Calendar.getInstance();
    c.add(Calendar.MONTH, 0);
    c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
    return StringToDate(getDateFormat(DateStyle.YYYY_MM_DD.getValue()).format(c.getTime()));
  }

  /**
   * Discription:[获取当前月的最后一天的日期]
   *
   * @return 当前月的最后一天
   *
   * Created on 2017/12/15
   * @author: 尹归晋
   */
  public static Date getlastDate() {
    Calendar c = Calendar.getInstance();
    c.add(Calendar.MONTH, 0);
    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
    return StringToDate(getDateFormat(DateStyle.YYYY_MM_DD.getValue()).format(c.getTime()));
  }

  /**
   * 获得该月第一天
   *
   * @author: jinhailong
   */
  public static String getFirstDayOfMonth(Date date, DateStyle dateStyle) {
    if (date == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    return DateToString(cal.getTime(), dateStyle);
  }

  /**
   * 获得该月最后一天
   *
   * @author: majianwei
   */
  public static String getLastDayOfMonth(Date date) {
    if (date == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    return DateToString(cal.getTime(), DateStyle.YYYY_MM_DD);
  }


  /**
   * 获得该月最后一天
   *
   * @author: jinhailong
   */
  public static String getLastDayOfMonth(Date date, DateStyle dateStyle) {
    if (date == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    return DateToString(cal.getTime(), dateStyle);
  }

  /**
   * 获得该月最后一天
   *
   * @author: jinhailong
   */
  public static Date getLastDayOfMonth1(Date date) {
    if (date == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    return cal.getTime();
  }

  /*
   * 将时间戳转换为时间
   */
  public static String stampToDate(long time){
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    String time_Date = sdf.format(new Date(time));
    return time_Date;

  }

  /**
   * Discription:[获取指定月的最后一天的日期]
   *
   * @return 指定的最后一天
   *
   * Created on 2017/12/15
   * @author: 尹归晋
   */
  public static int getLastDay(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.getActualMaximum(Calendar.DAY_OF_MONTH);
  }

  /**
   * Discription:[获取指定月的第一天的日期]
   *
   * @return 指定的第一天
   *
   * Created on 2017/12/15
   * @author: 尹归晋
   */
  public static int getFirstDay(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.getActualMinimum(Calendar.DAY_OF_MONTH);
  }

  /**
   * <p>Description:[获取当前日期字符串]</p>
   * Created on 2018/1/2
   *
   * @author 缪志红
   */
  public static String getCurrentDateString() {
    return DateToString(new Date(), DateStyle.YYYY_MM_DD);
  }

  /**
   * <p>Description:[获取当前日期]</p>
   * Created on 2018/1/2
   *
   * @author 缪志红
   */
  public static Date getCurrentDate() {
    return StringToDate(getCurrentDateString(), DateStyle.YYYY_MM_DD);
  }


  /**
   * 获取某月最后一天
   *
   * @param date wangjianying
   */
  public static Date changeDateToLastDateOfMonth(Date date) {
    if (date == null) {
      return null;
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * 获取当天凌晨时间
   *
   * @param date wangjianying
   */
  public static Date changeDateToWeeHours(Date date) {
    if (date == null) {
      return null;
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }




  public static List<Date> getAllTheDateOftheMonth(Date date) {
    List<Date> list = new ArrayList<>();
    if (date == null) {
      return list;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.DATE, 1);
    int month = cal.get(Calendar.MONTH);
    while (cal.get(Calendar.MONTH) == month) {
      list.add(cal.getTime());
      cal.add(Calendar.DATE, 1);
    }
    return list;
  }

  /**
   * @param judgeDay 是否判断天
   * @author zhangxiaopeng
   */
  public static boolean isSameDateByYearMonth(Date date1, Date date2, Boolean judgeDay) {
    try {
      boolean flag = true;
      Calendar cal1 = Calendar.getInstance();
      cal1.setTime(date1);
      Calendar cal2 = Calendar.getInstance();
      cal2.setTime(date2);
      boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
          .get(Calendar.YEAR);
      boolean isSameMonth = isSameYear
          && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
      flag = isSameMonth;
      if (judgeDay) {
        flag = isSameMonth && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
      }
      return flag;
    } catch (Exception e) {
      log.warn("DateUtils.isSameDateByYearMonth方法异常：", e);
      throw new RuntimeException("日期转换错误");
    }
  }

  public static Date getSpecifiedDayBefore(Date date) {//可以用new Date().toLocalString()传递参数
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    int day = c.get(Calendar.DATE);
    c.set(Calendar.DATE, day - 1);
    return c.getTime();
  }

  /**
   * 获得该月第一天
   *
   * @author: zhangxiaopeng
   */
  public static Date getFirstDayOfMonth(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return calendar.getTime();
  }

  /**
   * 当前日期减一个月
   *
   * @author: zhangxiaopeng
   */
  public static Date getSubtractionOneMonth(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.MONTH, -1);
    return calendar.getTime();
  }

  /**
   * 获得该月最后一天
   *
   * @author: zhangxiaopeng
   */
  public static Date getLastDayOfMonthReturnDate(Date date) {
    if (date == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    return cal.getTime();
  }

  /**
   * 判断该日期是否是该月的最后一天
   *
   * @param date 需要判断的日期
   * @author:zhangxiaopeng
   */
  public static boolean isLastDayOfMonth(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar.get(Calendar.DAY_OF_MONTH) == calendar
        .getActualMaximum(Calendar.DAY_OF_MONTH);
  }

  /**
   * 某年某月的最后一天
   *
   * @author:zhangxiaopeng
   */
  public static Date getLastDay(int year, int month) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DAY_OF_MONTH, 0); //关键！
    return cal.getTime();
  }

  /**
   * 获取一个时间里的所有月份的最后一天
   *
   * @author:zhangxiaopeng
   */
  public static List<String> getMonthBetween(String minDate, String maxDate) throws Exception {
    ArrayList<String> result = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//格式化为年月

    Calendar min = Calendar.getInstance();
    Calendar max = Calendar.getInstance();

    min.setTime(sdf.parse(minDate));
    min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

    max.setTime(sdf.parse(maxDate));
    max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

    while (min.before(max)) {
      result.add(sdf.format(min.getTime()));
      min.add(Calendar.MONTH, 1);
    }
    for (String s : result) {
      DateUtils.getLastDayOfMonthReturnDate(DateUtils.StringToDate(s));
    }
    return result;
  }

}