package com.murph9.moneybutdaily.model;

public enum DayType {
    None,
    Day,
    Week,
    Month,
    Quarterly,
    Year;

    public static final DayType[] CAN_SELECT = new DayType[] { Day, Week, Month, Quarterly, Year };
}
