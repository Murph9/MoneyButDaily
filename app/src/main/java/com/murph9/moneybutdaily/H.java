package com.murph9.moneybutdaily;

import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;

import org.joda.time.DateTime;

import java.text.DecimalFormat;

public class H {
    public static float GetRowPerDay(boolean isIncome, float amount, DateTime from, int lengthCount, DayType lengthType) {
        Row r = new Row();
        r.IsIncome = isIncome;
        r.Amount = amount;
        r.From = from;
        r.LengthCount = lengthCount;
        r.LengthType = lengthType;
        return r.CalcPerDay();
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
