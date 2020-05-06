package com.qiwen.base.util;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private DateUtil() {}

    public static Calendar wrap(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Date setToDayOfWeek(Date date, int dayOfWeek) {
        dayOfWeek = dayOfWeek == Calendar.SUNDAY ? 8 : dayOfWeek;
        Calendar calendar = wrap(date);
        int dow = calendar.get(Calendar.DAY_OF_WEEK);
        dow = dow == Calendar.SUNDAY ? 8 : dow;
        Date result = DateUtils.addDays(date, dayOfWeek - dow);
        return result;
    }

    /**
     * 计算两个日期之间相差了多少天, 如 2019-05-01 01:00:00 与 2019-05-03 00:30:00 之间的天数为 1, 此时 ceil 为 false, 标识向下取证,
     * 如果 ceil 为 true 则为 2.
     * @param startDate
     * @param endDate
     * @return
     */
//    public static int dateRange(Date startDate, Date endDate, boolean ceil) {
//        Date d1 = DateUtils.truncate(startDate, Calendar.DAY_OF_MONTH);
//        Date d2 = DateUtils.truncate(endDate, Calendar.DAY_OF_MONTH);
//        Date newExpectArrivalDate = new Date(tDate.getTime() + 30 * 60 * 1000);
//        double tInterval = (expectExtractDate.getTime() - newExpectArrivalDate.getTime()) / (24 * 60 * 60 * 1000.0);
//        int days = (int) Math.ceil(tInterval);
//    }
}
