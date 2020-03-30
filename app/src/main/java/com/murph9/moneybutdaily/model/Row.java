package com.murph9.moneybutdaily.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.time.LocalDateTime;

@Entity(tableName = "rows")
public class Row {

    @PrimaryKey(autoGenerate = true)
    public int Line;

    @ColumnInfo(name = "IsIncome")
    public boolean IsIncome;

    @NonNull
    @ColumnInfo(name = "From")
    public LocalDateTime From; //first day this applies

    @NonNull
    @ColumnInfo(name = "Amount")
    public float Amount; //base value

    @NonNull
    @ColumnInfo(name = "LengthCount")
    public int LengthCount; //count of type
    @NonNull
    @ColumnInfo(name = "LengthType")
    public DayType LengthType; //day,week,month,year...

    @NonNull
    @ColumnInfo(name = "Repeats")
    public boolean Repeats;

    @ColumnInfo(name = "LastDay")
    public LocalDateTime LastDay;

    @NonNull
    @ColumnInfo(name = "Category")
    public String Category; //categorising string

    @ColumnInfo(name = "Note")
    public String Note; //other words

    public Row() {
        LengthType = DayType.None;
    }

    public String Validate() {
        if (Amount <= 0)
            return "Amount is 0 or negative: " + Amount;
        if (From == null) //im not convinced of this error
            return "From start date not set";
        if (LengthCount == 0 && LengthType != DayType.Day)
            return "Length count cannot be 0, it is only valid for the day type.";
        if (LengthCount <= 0)
            return "Length count cannot be negative.";
        if (LengthType == DayType.None)
            return "Length type must not be None";
        if (LastDay != null && LastDay.isBefore(From))
            return "LastDay date earlier than the start date (From).";
        if (!Repeats && LastDay != null)
            return "Must be repeating for LastDay to be set.";

        return null;
    }

    public float CalcPerDay() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(From, CalcFirstPeriodEndDay()) + 1;
        if (days <= 0) days = 1; // if no days 'assume' its 1 day
        return (IsIncome ? Amount : -Amount) / days;
    }

    public LocalDateTime CalcFirstPeriodEndDay() {

        LocalDateTime result = this.From;
        switch (this.LengthType) {
            case Day:
                result = result.plusDays(this.LengthCount);
                break;
            case Week:
                result = result.plusDays(this.LengthCount * 7);
                break;
            case Month:
                result = result.plusMonths(this.LengthCount);
                break;
            case Quarterly:
                result = result.plusMonths(this.LengthCount * 3);
                break;
            case Year:
                result = result.plusYears(this.LengthCount);
                break;
            default:
                try {
                    throw new Exception("huh, no length type");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        return result.plusDays(-1);
    }

    //Including repeating, the last day it applies + 1 (can be DateTimeOffset.MaxValue or null)
    public LocalDateTime CalcLastDay()
    {
        if (!Repeats) {
            // then use the end of the period
            return CalcFirstPeriodEndDay();
        }

        //else its forever or if its set
        return LastDay == null ? LocalDateTime.MAX : LastDay;
    }

    @Override
    public String toString()
    {
        return Category + ": " + CalcPerDay();
    }

    public String toExportString() {
        Object[] list = new Object[] { this.Amount, this.LengthCount, this.LengthType,
                this.Repeats, this.LastDay, this.Category, this.Note };
        return TextUtils.join(", ", list);
    }
}

