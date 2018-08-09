package com.murph9.moneybutdaily.model;

import com.murph9.moneybutdaily.H;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public class DayTypePeriod {

    public final DayType type;
    public final DateTime date;

    public DayTypePeriod(DayType type, DateTime date) {
        this.type = type;
        this.date = date;
    }

    public String dateRangeFor() {
        switch (type) {
            case Day:
                return date.toString(H.VIEW_YMD_FORMAT);
            case Week:
                return H.startOfWeek(date).toString(H.VIEW_YMD_FORMAT) + " - " + H.startOfWeek(date).plusDays(7).toString(H.VIEW_YMD_FORMAT);
            case Month:
                return date.toString(H.VIEW_YM_FORMAT);
            case Quarterly:
                return date.toString(H.VIEW_YM_FORMAT) + " - " + date.plusMonths(3).toString(H.VIEW_YM_FORMAT);
            case Year:
                return date.toString(H.VIEW_Y_FORMAT);
            case None:
            default:
                return "<unknown>";
        }
    }

    public boolean contains(DateTime test) {
        return new Interval(date, nextPeriod(1).date.minus(1)).contains(test);
    }
    public boolean isBefore(DateTime test) {
        return nextPeriod(1).date.compareTo(test.withTimeAtStartOfDay()) > 0;
    }

    public DayTypePeriod nextPeriod(int offset) {
        DateTime newDate;
        switch (type) {
            case Day:
                newDate = date.plusDays(offset);
                break;
            case Week:
                newDate = date.plusWeeks(offset);
                break;
            case Month:
                newDate = date.plusMonths(offset);
                break;
            case Quarterly:
                newDate = date.plusMonths(offset * 3);
                break;
            case Year:
                newDate = date.plusYears(offset);
                break;
            case None:
            default:
                throw new IllegalArgumentException("type");
        }

        return new DayTypePeriod(this.type, newDate);
    }

    //region Static Methods
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

    public static int dayCountByType(DayType type) {
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
    //endregion
}
