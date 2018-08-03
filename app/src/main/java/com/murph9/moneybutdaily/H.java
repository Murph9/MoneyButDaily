package com.murph9.moneybutdaily;

import com.murph9.moneybutdaily.model.DayType;

import org.joda.time.DateTime;

import java.text.DecimalFormat;

public class H {

    public static final String VIEW_YMD_FORMAT = "yyyy/MM/dd";
    public static final String VIEW_YM_FORMAT = "yyyy/MM";
    public static final String VIEW_Y_FORMAT = "yyyy";

    public static DateTime startOfWeek(DateTime date) {
        return date.weekOfWeekyear().roundFloorCopy();
    }
    public static DateTime startOfMonth(DateTime date) {
        return date.monthOfYear().roundFloorCopy();
    }
    public static DateTime startOfYear(DateTime date) {
        return date.dayOfYear().roundFloorCopy();
    }

    public static String dateRangeFor(DateTime from, DayType type) {
        switch(type) {
            case Day:
                return from.toString(H.VIEW_YMD_FORMAT);
            case Week:
                return startOfWeek(from).toString(H.VIEW_YMD_FORMAT) + " - " +startOfWeek(from).plusDays(7).toString(H.VIEW_YMD_FORMAT);
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

    private static final DecimalFormat valueFormat = new DecimalFormat("#.##");
    public static String to2Places(float value) {
        return valueFormat.format(value);
    }

    public static int ceilWithFactor(float value, int factor) {
        return (int)Math.ceil(value/(float)factor)*factor;
    }
    public static int floorWithFactor(float value, int factor) {
        return (int)Math.floor(value/(float)factor)*factor;
    }
}
