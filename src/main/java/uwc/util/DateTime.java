package uwc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by steven on 05/07/2017.
 */

public class DateTime {

    public static long now() {
        return new Date().getTime();
    }

    /**
     * 判断当前日期是星期几
     */
    public static int dayForWeek(String pTime) {
        if (pTime == null) {
            return 0;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(pTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int dayForWeek = 0;
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            dayForWeek = 7;
        } else {
            dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        }
        return dayForWeek;
    }

    /**
     * 毫秒值转换为时间 -带汉字
     */
    public static String getDate2(String date) {
        if (date == null) {
            return "";
        }
        long ms = Long.parseLong(date);
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 毫秒值转换为时间 -带汉字
     */
    public static String getDateChina(String date) {
        if (date == null) {
            return "";
        }
        long ms = Long.parseLong(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 毫秒值转换为时间
     */
    public static String getDate(String date) {
        if (date == null) {
            return "";
        }
        long ms = Long.parseLong(date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 毫秒值转换为时间---HH:mm
     */
    public static String getMinDate(String date) {
        if (date == null) {
            return "";
        }
        long ms = Long.parseLong(date);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 毫秒值转换为时间---MM月 dd日 HH:mm
     */
    public static String getMonthDate(String date) {
        if (date == null) {
            return "";
        }
        long ms = Long.parseLong(date);
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日 HH:mm");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 根据提供的时间戳转换星期几
     *
     * @param timeStamp
     * @return
     */
    public static String getWeek(long timeStamp) {
        int mydate = 0;
        String week = null;
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(timeStamp));
        mydate = cd.get(Calendar.DAY_OF_WEEK);
        // 获取指定日期转换成星期几
        if (mydate == 1) {
            week = "周日";
        } else if (mydate == 2) {
            week = "周一";
        } else if (mydate == 3) {
            week = "周二";
        } else if (mydate == 4) {
            week = "周三";
        } else if (mydate == 5) {
            week = "周四";
        } else if (mydate == 6) {
            week = "周五";
        } else if (mydate == 7) {
            week = "周六";
        }
        return week;
    }

    /**
     * 获取当月第一天的时间戳
     */
    public static long getMonthFirstDay() {
        Calendar calendar = Calendar.getInstance();// 获取当前日期
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        System.out.println(calendar.getTimeInMillis());

        return calendar.getTimeInMillis();
    }

    /**
     * 时间值转换为毫秒
     */
    public static long getLongDate(String date) {
        if (date == null) {
            return 0;
        }
        long s = 0;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");// 初始化Formatter的转换格式。
        try {
            s = formatter.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 精确时间值转换为毫秒
     */
    public static long getLongTrueDate(String date) {
        if (date == null) {
            return 0;
        }
        long s = 0;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");// 初始化Formatter的转换格式。
        try {
            s = formatter.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 当前时间日
     */
    public static String getCurrentDay() {
        long ms = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("dd");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 当前时间小时
     */
    public static String getCurrentHour() {
        long ms = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("HH");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 当前时间分钟
     */
    public static String getCurrentMin() {
        long ms = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("mm");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 当前时间月
     */
    public static String getCurrentMonth() {
        long ms = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("MM");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 当前时间年
     */
    public static String getCurrentYear() {
        long ms = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");// 初始化Formatter的转换格式。
        String hms = formatter.format(ms);
        return hms;
    }

    /**
     * 当前月有多少天
     */
    public static String[] getDays(String year, String month) {
        String[] days = null;
        int y = Integer.parseInt(year);
        int m = Integer.parseInt(month);
        if (m == 1 || m == 3 || m == 5 || m == 7 || m == 8 || m == 10 || m == 12) {
            days = new String[31];
            for (int i = 0; i < 31; i++) {
                days[i] = (i + 1) + "日";
            }
            return days;
        } else if (m == 4 || m == 6 || m == 9 || m == 11) {
            days = new String[30];
            for (int i = 0; i < 30; i++) {
                days[i] = (i + 1) + "日";
            }
            return days;
        } else if (m == 2) {
            if ((y % 100 == 0 && y % 400 == 0) || (y % 100 != 0 && y % 4 == 0)) {
                days = new String[29];
                for (int i = 0; i < 29; i++) {
                    days[i] = (i + 1) + "日";
                }
                return days;
            } else {
                days = new String[28];
                for (int i = 0; i < 28; i++) {
                    days[i] = (i + 1) + "日";
                }
                return days;
            }
        }
        return days;
    }
}
