package com.murph9.moneybutdaily.model;

import java.util.Arrays;
import java.util.List;

public enum DayType {
    None,
    Day,
    Week,
    Month,
    Quarterly,
    Year;

    public static final List<DayType> CAN_SELECT = Arrays.asList(Day, Week, Month, Quarterly, Year);
}
