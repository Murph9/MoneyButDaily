package com.murph9.moneybutdaily;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class H {

    public static final String VIEW_YMD_FORMAT = "yyyy/MM/dd";
    public static final String VIEW_YM_FORMAT = "yyyy/MM";
    public static final String VIEW_Y_FORMAT = "yyyy";

    public static final String VIEW_MD_FORMAT = "MMM-d";
    public static final String VIEW_YM_S_FORMAT = "yy-MMM";

    public static LocalDateTime getStartOfDay(LocalDateTime date) {
        return date.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public static String formatDate(LocalDateTime date, String format) {
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime startOfWeek(LocalDateTime date) {
        return date.with(java.time.DayOfWeek.MONDAY);
    }
    public static LocalDateTime startOfMonth(LocalDateTime date) {
        return date.withDayOfMonth(1);
    }
    public static LocalDateTime startOfYear(LocalDateTime date) {
        return date.withDayOfYear(1);
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

    //https://stackoverflow.com/a/11648106/9353639
    public static <K,V extends Comparable<? super V>> List<Map.Entry<K, V>> entriesSortedByValue(Map<K,V> map, final boolean desc) {

        List<Map.Entry<K,V>> sortedEntries = new ArrayList<>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K,V>>() {
                    @Override
                    public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        if (desc)
                            return e2.getValue().compareTo(e1.getValue());
                        else
                            return e1.getValue().compareTo(e2.getValue());
                    }
                }
        );
        return sortedEntries;
    }
}
