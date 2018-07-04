package com.murph.moneybutdaily.database;

import android.arch.persistence.room.TypeConverter;

import com.murph.moneybutdaily.model.DayType;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

public class Converters {
    @TypeConverter
    public static DateTime longToDateTime(Long value) {
        if (value == null)
            return null;

        DateTime dt = new DateTime();
        dt.plusMillis(value.intValue());
        return dt;
    }

    @TypeConverter
    public static Long dateTimeToLong(DateTime date) {
        return date == null ? null : date.getMillis();
    }

    @TypeConverter
    public static DayType fromString(String dayType) { return dayType == null ? DayType.None : Enum.valueOf(DayType.class, dayType); }

    @TypeConverter
    public static String fromDayType(DayType dayType) { return dayType == null ? null : dayType.name(); }
}
