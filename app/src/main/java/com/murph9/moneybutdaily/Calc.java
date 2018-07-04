package com.murph9.moneybutdaily;

import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Calc {
    private final List<Range> _dayRanges = new LinkedList<>();
    private final List<String> _categories = new LinkedList<>();
    public Collection<String> GetCategories()
    {
        return _categories;
    }

    private class Range {
        DateTime dt1;
        DateTime dt2;
        DateTime dt3;
        Row row;

        Range(DateTime dt1, DateTime dt2, DateTime dt3, Row row) {
            this.dt1 = dt1;
            this.dt2 = dt2;
            this.dt3 = dt3;
            this.row = row;
        }
    }

    private DateTime _firstEntryDate;

    public Calc(Collection<Row> data) {
        _firstEntryDate = new DateTime(Long.MIN_VALUE);

        for (Row r: data) {
            _dayRanges.add(new Range(r.From, r.CalcFirstPeriodEndDay(), r.CalcLastDay(), r));

            if (!_categories.contains(r.Category))
                _categories.add(r.Category);

            //get min date
            _firstEntryDate = _firstEntryDate.compareTo(r.From) > 0 ? r.From : _firstEntryDate;
        }
    }

    private Collection<Row> RowsForDay(DateTime day)
    {
        Collection<Row> list = new LinkedList<>();
        for (Range range: _dayRanges)
        {
            //current day is before day to check, ignore
            if (range.dt1.compareTo(day) > 0)
                continue;

            // repeat checking, if there is no repeat set and the end date is after, ignore
            // the period is equal to the first day and the day before the last day
            if ((range.row.RepeatCount == 0 || range.row.RepeatType == DayType.None) && range.dt2.compareTo(day) <= 0)
                continue;

            // also include repeat range last day start
            if (range.dt3 != null && range.dt3.compareTo(day) <= 0)
                continue;

            list.add(range.row);
        }

        return list;
    }

    public float TotalForDay(DateTime day)
    {
        float total = 0;
        for (Row row: RowsForDay(day)) {
            total += row.CalcPerDay();
        }
        return total;
    }

    public float TotalForWeek(DateTime day)
    {
        //get start day of the week (monday)
        day = day.weekOfWeekyear().roundFloorCopy();

        float total = 0;
        total += TotalForDay(day);
        total += TotalForDay(day.plusDays(1));
        total += TotalForDay(day.plusDays(2));
        total += TotalForDay(day.plusDays(3));
        total += TotalForDay(day.plusDays(4));
        total += TotalForDay(day.plusDays(5));
        total += TotalForDay(day.plusDays(6));

        return total;
    }

    public float TotalForMonth(DateTime day)
    {
        //get the start of the month
        day = day.monthOfYear().roundFloorCopy();

        int month = day.getMonthOfYear();
        float total = 0;
        while (day.getMonthOfYear() == month)
        {
            total += TotalForDay(day);
            day = day.plusDays(1);
        }

        return total;
    }

    public Map<String, Float> ReportForDay(DateTime day)
    {
        Collection<Row> rows = RowsForDay(day);
        Map<String, Float> dict = new HashMap<>();
        for (Row row: rows)
        {
            if (!dict.containsKey(row.Category))
                dict.put(row.Category, 0f);

            dict.put(row.Category, dict.get(row.Category) + row.CalcPerDay());
        }

        return dict;
    }

    //PERF: kind of slow
    public Map<String, Float> ReportForWeek(DateTime day)
    {
        //get start day of the week (monday)
        day = day.weekOfWeekyear().roundFloorCopy();

        Map<String, Float> dict = ReportForDay(day);

        day = day.plusDays(1);
        while (day.dayOfWeek().get() != DateTimeConstants.MONDAY)
        {
            Map<String, Float> newDayDict = ReportForDay(day);

            for (String key: newDayDict.keySet())
            {
                if (!dict.containsKey(key))
                    dict.put(key, 0f);

                dict.put(key, dict.get(key) + newDayDict.get(key));
            }

            day = day.plusDays(1);
        }

        return dict;
    }

    //PERF: slow
    public Map<String, Float> ReportForMonth(DateTime day)
    {
        //get the start of the month
        day = day.monthOfYear().roundFloorCopy();

        Map<String, Float> dict = ReportForDay(day);

        day = day.plusDays(1);
        while (day.dayOfMonth().get() != 1) //haha do while
        {
            Map<String, Float> newDayDict = ReportForDay(day);

            for (String key: newDayDict.keySet())
            {
                if (!dict.containsKey(key))
                    dict.put(key, 0f);

                dict.put(key, dict.get(key) + newDayDict.get(key));
            }

            day = day.plusDays(1);
        }

        return dict;
    }

    //PERF: really slow (causes way too many maps)
    public Map<String, Float> ReportForYear(DateTime day)
    {
        //get the start of the year
        day = day.dayOfYear().roundFloorCopy();

        int year = day.getYear();
        Map<String, Float> dict = ReportForMonth(day);

        day = day.plusMonths(1);
        while (day.getYear() == year) //haha do while
        {
            Map<String, Float> newDayDict = ReportForMonth(day);

            for (String key: newDayDict.keySet())
            {
                if (!dict.containsKey(key))
                    dict.put(key, 0f);

                dict.put(key, dict.get(key) + newDayDict.get(key));
            }

            day = day.plusMonths(1);
        }

        return dict;
    }
}
