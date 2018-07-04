package com.murph.moneybutdaily.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Days;

@Entity(tableName = "rows")
public class Row {

    @PrimaryKey(autoGenerate = true)
    public int Line;

    @NonNull
    @ColumnInfo(name = "IsIncome")
    public boolean IsIncome;

    @NonNull
    @ColumnInfo(name = "From")
    public DateTime From; //first day this applies

    @NonNull
    @ColumnInfo(name = "Amount")
    public float Amount; //base value

    @NonNull
    @ColumnInfo(name = "LengthCount")
    public int LengthCount; //count of type
    @NonNull
    @ColumnInfo(name = "LengthType")
    public DayType LengthType; //day,week,month,year...

    @ColumnInfo(name = "RepeatCount")
    public int RepeatCount; //count of repeat type

    @NonNull
    @ColumnInfo(name = "RepeatType")
    public DayType RepeatType; //day,week,month,year...

    @ColumnInfo(name = "RepeatEnd")
    public DateTime RepeatEnd; //no more repeats can start after this day (i.e. 'LastRepeatDay')

    @NonNull
    @ColumnInfo(name = "Category")
    public String Category; //categorising string

    @ColumnInfo(name = "Note")
    public String Note; //other words

    public Row() {
        LengthType = DayType.None;
        RepeatType = DayType.None;
    }

    //TODO use before saving
    public void Validate() throws Exception {
        if (Amount <= 0)
            throw new Exception("Amount is 0 or negative: " + Amount);
        if (From == null) //im not convinced of this error
            throw new Exception("From start date not set");
        if (LengthCount == 0 && LengthType != DayType.Day)
            throw new Exception("Length count cannot be 0, it is only valid for the day type.");
        if (LengthCount <= 0)
            throw new Exception("Length count cannot be negative.");
        if (LengthType == DayType.None)
            throw new Exception("Length type must not be None");
        if ((RepeatCount != 0 && RepeatType == DayType.None) || (RepeatCount == 0 && RepeatType != DayType.None))
            throw new Exception("Only one of the repeat type fields is set");
        if (RepeatCount != 0 && RepeatEnd != null && RepeatEnd.isBefore(From))
            throw new Exception("RepeatEnd date earlier than the start date (From).");

        //TODO some less awesome validations because date calculations are hard:
        // (if something lasts a day but repeats weekly (1d_1w), it does NOT handle it, so remove these cases..)
        if (RepeatCount != 0 || RepeatType != DayType.None)
        {
            if (LengthType != RepeatType)
                throw new Exception("Length type and Repeat type must be the same.");
            if (LengthCount != RepeatCount)
                throw new Exception("Length count and Repeat count must be the same.");
        }
    }

    public float CalcPerDay() {
        return (IsIncome ? Amount : -Amount) / Days.daysBetween(From, CalcFirstPeriodEndDay()).getDays();
    }

    public DateTime CalcFirstPeriodEndDay() {

        DateTime result = this.From;

        switch (this.LengthType)
        {
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

        return result;
    }

    //Including repeating, the last day it applies + 1 (can be DateTimeOffset.MaxValue or null)
    public DateTime CalcLastDay()
    {
        if (RepeatCount == 0 || RepeatType == DayType.None)
            return null;

        if (RepeatEnd == null)
            return RepeatEnd;

        //loop through the start days of each period
        DateTime result = From;
        int dayDiff = Days.daysBetween(From, CalcFirstPeriodEndDay()).getDays();
        while (result.isBefore(RepeatEnd.plusDays(1)))
        {
            result = result.plusDays(dayDiff);
        }

        return result;
    }

    @Override
    public String toString()
    {
        return Category + ": " + CalcPerDay();
    }
}

