package com.murph9.moneybutdaily.database;

import android.arch.persistence.room.TypeConverter;

import com.murph9.moneybutdaily.model.DayType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Converters {
    @TypeConverter
    public static LocalDateTime longToDateTime(Long value) {
        if (value == null)
            return null;

        return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @TypeConverter
    public static Long dateTimeToLong(LocalDateTime date) {
        if (date == null) return null;

        return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @TypeConverter
    public static DayType fromString(String dayType) { return dayType == null ? DayType.None : Enum.valueOf(DayType.class, dayType); }

    @TypeConverter
    public static String fromDayType(DayType dayType) { return dayType == null ? null : dayType.name(); }
}
