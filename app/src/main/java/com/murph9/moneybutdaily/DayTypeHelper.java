package com.murph9.moneybutdaily;

import com.murph9.moneybutdaily.model.DayType;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public class DayTypeHelper {

    public static String dateFormatByType(DayType type) {
        switch(type) {
            case Day:
            case Week:
                return H.VIEW_MD_FORMAT;
            case Month:
            case Quarterly:
                return H.VIEW_YM_S_FORMAT;
            case Year:
                return H.VIEW_Y_FORMAT;
            case None:
            default:
                throw new IllegalArgumentException("type");
        }
    }
    public static String dateRangeFor(DateTime from, DayType type) {
        switch(type) {
            case Day:
                return from.toString(H.VIEW_YMD_FORMAT);
            case Week:
                return H.startOfWeek(from).toString(H.VIEW_YMD_FORMAT) + " - " + H.startOfWeek(from).plusDays(7).toString(H.VIEW_YMD_FORMAT);
            case Month:
                return from.toString(H.VIEW_YM_FORMAT);
            case Quarterly:
                return from.toString(H.VIEW_YM_FORMAT) + " - " + from.plusMonths(3).toString(H.VIEW_YM_FORMAT);
            case Year:
                return from.toString(H.VIEW_Y_FORMAT);
            case None:
            default:
                return "<unknown>";
        }
    }

    public static DateTime offsetDateByType(DayType type, DateTime from, int offset) {
        switch(type) {
            case Day:
                return from.plusDays(offset);
            case Week:
                return from.plusWeeks(offset);
            case Month:
                return from.plusMonths(offset);
            case Quarterly:
                return from.plusMonths(offset*3);
            case Year:
                return from.plusYears(offset);
            case None:
            default:
                throw new IllegalArgumentException("type");
        }
    }

    public static float totalByType(DayType type, Calc calc, DateTime from) {
        return calc.TotalFor(from, type);
    }

    public static boolean isCurrentByType(DayType type, DateTime start, DateTime test) {
        return new Interval(start, offsetDateByType(type, start, 1).minus(1)).contains(test);
    }
    public static boolean isFutureByType(DayType type, DateTime start, DateTime test) {
        return offsetDateByType(type, start, 1).compareTo(test.withTimeAtStartOfDay()) > 0;
    }

    public static int closeDayCountByType(DayType type) {
        switch(type) {
            case Day:
                return 1;
            case Week:
                return 7;
            case Month:
                return 30; //31,(28|29),31,30,31,30,31,31,30,31,30,31
            case Quarterly:
                return 91; //(90|91),91,92,92
            case Year:
                return 365; //(365|366)
            case None:
            default:
                throw new IllegalArgumentException("type");
        }
    }
}
