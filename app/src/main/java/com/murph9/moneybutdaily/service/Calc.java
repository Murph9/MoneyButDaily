package com.murph9.moneybutdaily.service;

import com.murph9.moneybutdaily.H;
import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.DayTypePeriod;
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
    public List<String> GetCategories()
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

    private DateTime firstEntryDate;

    private Cache<DateTime, Collection<Row>> rowDayCache;

    public Calc(Collection<Row> data) {
        rowDayCache = new Cache<>();
        firstEntryDate = new DateTime(Long.MIN_VALUE);

        if (data == null) {
            data = new LinkedList<>();
        }

        for (Row r: data) {
            _dayRanges.add(new Range(r.From, r.CalcFirstPeriodEndDay(), r.CalcLastDay(), r));

            if (!_categories.contains(r.Category))
                _categories.add(r.Category);

            //get min date
            firstEntryDate = firstEntryDate.compareTo(r.From) > 0 ? r.From : firstEntryDate;
        }
    }

    private Collection<Row> RowsForDay(DateTime day) {
        Collection<Row> cacheValue = rowDayCache.get(day.withTimeAtStartOfDay());
        if (cacheValue != null) {
            return cacheValue;
        }

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

        rowDayCache.set(day.withTimeAtStartOfDay(), list);
        return list;
    }

    public float TotalFor(DayTypePeriod period) {
        float total = 0;
        for (Map.Entry<String, Float> entry: ReportFor(period).entrySet()) {
            total += entry.getValue();
        }
        return total;
    }

    public Map<String, Float> ReportFor(DayTypePeriod period) {
        Map<String, Float> dict;
        switch (period.type) {
            case Day:
                dict = reportForDay(period.date);
                break;
            case Week:
                dict = reportForWeek(period.date);
                break;
            case Month:
                dict = reportForMonth(period.date);
                break;
            case Year:
                dict = reportForYear(period.date);
                break;
            case Quarterly:
            case None:
            default:
                dict = new HashMap<>();
        }
        return dict;
    }

    private Map<String, Float> reportForDay(DateTime day) {
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
    private Map<String, Float> reportForWeek(DateTime day) {
        //get start day of the week (monday)
        day = H.startOfWeek(day);

        Map<String, Float> dict = reportForDay(day);

        day = day.plusDays(1);
        while (day.dayOfWeek().get() != DateTimeConstants.MONDAY)
        {
            Map<String, Float> newDayDict = reportForDay(day);

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
    private Map<String, Float> reportForMonth(DateTime day) {
        //get the start of the month
        day = H.startOfMonth(day);

        Map<String, Float> dict = reportForDay(day);

        day = day.plusDays(1);
        while (day.dayOfMonth().get() != 1) //haha do while
        {
            Map<String, Float> newDayDict = reportForDay(day);

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

    //PERF: really slow (probably causes way too many map<>s)
    private Map<String, Float> reportForYear(DateTime day) {
        //get the start of the year
        day = H.startOfYear(day);

        int year = day.getYear();
        Map<String, Float> dict = reportForMonth(day);

        day = day.plusMonths(1);
        while (day.getYear() == year) //haha do while
        {
            Map<String, Float> newDayDict = reportForMonth(day);

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


    public DateTime getFirstDate() {
        return this.firstEntryDate;
    }
}
