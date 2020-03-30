package com.murph9.moneybutdaily;

import com.murph9.moneybutdaily.service.Calc;
import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.DayTypePeriod;
import com.murph9.moneybutdaily.model.Row;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CalcTest {

    private static float DELTA = 0.0001f;

    //region TestData
    private static final Row Test9_1Day_None = new Row() {
        { // 9,0
            From = LocalDateTime.of(2018, 1, 1, 0, 0);
            Amount = 9;
            LengthCount = 1;
            LengthType = DayType.Day;
            Category = "1d_n";
        }
    };
    private static final Row Test26_2Day_None = new Row() {
        { // 0,13,13,0
            From = LocalDateTime.of(2018, 1, 2, 0, 0);
            Amount = 26;
            LengthCount = 2;
            LengthType = DayType.Day;
            Category = "2d_n";
        }
    };
    private static final Row Test14_2Day_2Day = new Row() {
        {// 0,7,7,7,7,7,7,0
            From = LocalDateTime.of(2018, 1, 2, 0, 0);
            Amount = 14;
            LengthCount = 2;
            LengthType = DayType.Day;
            Repeats = true;
            LastDay = LocalDateTime.of(2018, 1, 10, 0, 0);
            Category = "2d_2d";
        }
    };
    private static final Row Test12Daily_Year_Year = new Row() {
        { // 0,2,2,2...
            From = LocalDateTime.of(2018, 1, 3, 0, 0);
            Amount = 365*12;
            LengthCount = 1;
            LengthType = DayType.Year;
            Repeats = true;
            Category = "1y_1y";
            IsIncome = true;
            LastDay = LocalDateTime.of(2018, 1, 29, 0, 0);
        }
    };
    //endregion

    @Test
    public void SimpleDay() {
        Row row = Test9_1Day_None;
        String result = row.Validate();
        assertNull(result);

        assertEquals(row.CalcPerDay(), -9f, DELTA);
        assertEquals(row.CalcFirstPeriodEndDay(), row.From);
    }

    @Test
    public void SimpleYear() {
        Row row = Test12Daily_Year_Year;
        String result = row.Validate();
        assertNull(result);

        assertEquals(row.CalcPerDay(), 12f, DELTA);
        assertEquals(LocalDateTime.of(2019, 1, 2, 0, 0), row.CalcFirstPeriodEndDay());
    }

    @Test
    public void Large()
    {
        Calc calc = new Calc(Arrays.asList(Test9_1Day_None,
                Test26_2Day_None,
                Test14_2Day_2Day,
                Test12Daily_Year_Year));

        //assert some things
        assertTrue("A category is missing", calc.GetCategories().contains("1d_n"));

        assertEquals(0, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2017, 12, 31, 0, 0))), DELTA);
        assertEquals(-9, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 1, 0, 0))), DELTA);
        assertEquals(-20, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 2, 0, 0))), DELTA);
        assertEquals(-8, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 3, 0, 0))), DELTA);
        assertEquals(5, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 4, 0, 0))), DELTA);
        assertEquals(5, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 5, 0, 0))), DELTA);
        assertEquals(5, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 6, 0, 0))), DELTA);
        assertEquals(5, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 7, 0, 0))), DELTA);
        assertEquals(5, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 8, 0, 0))), DELTA);
        assertEquals(5, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 9, 0, 0))), DELTA);
        assertEquals(5, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 10, 0, 0))), DELTA);
        assertEquals(12, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 11, 0, 0))), DELTA);
        assertEquals(12, calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018, 1, 12, 0, 0))), DELTA);

        //week
        assertEquals(-17, calc.TotalFor(new DayTypePeriod(DayType.Week, LocalDateTime.of(2018,1,1,0,0))), DELTA);
        assertEquals(63, calc.TotalFor(new DayTypePeriod(DayType.Week, LocalDateTime.of(2018,1,8,0,0))), DELTA);
        assertEquals(84, calc.TotalFor(new DayTypePeriod(DayType.Week, LocalDateTime.of(2018,1,15,0,0))), DELTA);

        //month
        assertEquals(226, calc.TotalFor(new DayTypePeriod(DayType.Month, LocalDateTime.of(2018,1,1,0,0))), DELTA);
        assertEquals(0, calc.TotalFor(new DayTypePeriod(DayType.Month, LocalDateTime.of(2018,2,1,0,0))), DELTA);
    }


    @Test
    public void Income() {
        Calc calc = new Calc(Arrays.asList(
            new Row()
            {
                {
                    From = LocalDateTime.of(2018,5,16,0,0);
                    Amount = 1000;
                    LengthCount = 4;
                    LengthType = DayType.Week;
                    Category = "income?";
                    IsIncome = true;
                    Repeats = true;
                    LastDay = LocalDateTime.of(2019,1,1,0,0);
                }
            },
            new Row()
            {
                {
                    From = LocalDateTime.of(2018,5,26,0,0);
                    Amount = 100;
                    LengthCount = 4;
                    LengthType = DayType.Week;
                    Category = "food";
                    IsIncome = false;
                    Repeats = true;
                    LastDay = LocalDateTime.of(2019,1,1,0,0);
                }
            })
        );

        float incomeDay = 1000 / 28f;
        float foodDay = 100 / 28f;

        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018,6,1,0,0))), incomeDay - foodDay, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018,6,30,0,0))), incomeDay - foodDay, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.of(2018,7,1,0,0))), incomeDay - foodDay, DELTA);

        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Week, LocalDateTime.of(2018,6,1,0,0))), 7 * (incomeDay - foodDay), DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Week, LocalDateTime.of(2018,7,1,0,0))), 7 * (incomeDay - foodDay), DELTA);

        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Month, LocalDateTime.of(2018,7,1,0,0))), 31 * (incomeDay - foodDay), DELTA*10); //31 days in july (with more error)
    }
}
