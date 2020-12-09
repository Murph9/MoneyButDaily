package com.murph9.moneybutdaily.service;

import android.graphics.Color;

import com.murph9.moneybutdaily.H;
import com.murph9.moneybutdaily.model.DayTypePeriod;
import com.murph9.moneybutdaily.model.Row;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Calc {

    private final List<Range> _dayRanges = new LinkedList<>();
    private final List<String> _categories = new LinkedList<>();

    public List<String> GetCategories()
    {
        return _categories;
    }

    private Cache<LocalDateTime, Collection<Row>> rowDayCache;

    private static class Range {
        LocalDateTime start;
        LocalDateTime end;
        Row row;

        Range(LocalDateTime start, LocalDateTime end, Row row) {
            this.start = start;
            this.end = end;
            this.row = row;
        }

        boolean inRange(LocalDateTime time) {
            if (start.isEqual(time) || end.isEqual(time))
                return true; //boundary check says yes
            return start.isBefore(time) && end.isAfter(time);
        }
    }

    public Calc(Collection<Row> data) {
        rowDayCache = new Cache<>();

        if (data == null) {
            data = new LinkedList<>();
        }

        for (Row r: data) {
            _dayRanges.add(new Range(r.From, r.CalcLastDay(), r));

            if (!_categories.contains(r.Category))
                _categories.add(r.Category);
        }
    }

    private Collection<Row> RowsForDay(LocalDateTime day) {
        Collection<Row> cacheValue = rowDayCache.get(day);
        if (cacheValue != null) {
            return cacheValue;
        }

        Collection<Row> list = new LinkedList<>();
        for (Range range : _dayRanges) {
            if (!range.inRange(day))
                continue;

            list.add(range.row);
        }

        rowDayCache.set(day, list);
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

    private Map<String, Float> reportForDay(LocalDateTime day) {
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
    private Map<String, Float> reportForWeek(LocalDateTime day) {
        // get start day of the week (monday)
        day = H.startOfWeek(day);

        Map<String, Float> dict = reportForDay(day);

        day = day.plusDays(1);
        while (day.getDayOfWeek() != java.time.DayOfWeek.MONDAY)
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
    private Map<String, Float> reportForMonth(LocalDateTime day) {
        day = H.startOfMonth(day);

        Map<String, Float> dict = reportForDay(day);

        day = day.plusDays(1);
        while (day.getDayOfMonth() != 1) //haha do while
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
    private Map<String, Float> reportForYear(LocalDateTime day) {
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
}
