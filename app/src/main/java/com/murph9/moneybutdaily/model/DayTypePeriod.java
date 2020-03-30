package com.murph9.moneybutdaily.model;

import com.murph9.moneybutdaily.H;

import java.time.LocalDateTime;

public class DayTypePeriod {

    public final DayType type;
    public final LocalDateTime date;

    public DayTypePeriod(DayType type, LocalDateTime date) {
        this.type = type;
        this.date = date;
    }

    public String dateRangeFor() {
        switch (type) {
            case Day:
                return H.formatDate(date, H.VIEW_YMD_FORMAT);
            case Week:
                return H.formatDate(H.startOfWeek(date), H.VIEW_YMD_FORMAT) + " - " + H.formatDate(H.startOfWeek(date).plusDays(6), H.VIEW_YMD_FORMAT);
            case Month:
                return H.formatDate(date, H.VIEW_YM_FORMAT);
            case Quarterly:
                return H.formatDate(date, H.VIEW_YM_FORMAT) + " - " + H.formatDate(date.plusMonths(2), H.VIEW_YM_FORMAT);
            case Year:
                return H.formatDate(date, H.VIEW_Y_FORMAT);
            case None:
            default:
                return "<unknown>";
        }
    }

    public boolean contains(LocalDateTime test) {
        return this.date.isBefore(test) && nextPeriod(1).date.minusDays(1).isAfter(test);
    }
    public boolean isAfter(LocalDateTime test) {
        return nextPeriod(1).date.isAfter(test);
    }

    public DayTypePeriod nextPeriod(int offset) {
        if (offset == 0) //no need for math if its 0
            return new DayTypePeriod(this.type, this.date);

        LocalDateTime newDate;
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
