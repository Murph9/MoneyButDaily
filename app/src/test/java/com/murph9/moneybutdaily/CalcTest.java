package com.murph9.moneybutdaily;

import android.test.suitebuilder.annotation.SmallTest;

import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.DayTypePeriod;
import com.murph9.moneybutdaily.model.Row;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.tz.UTCProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

@SmallTest
public class CalcTest {

    private static float DELTA = 0.0001f;

    //region TestData
    private static final Row Test9_1Day_None = new Row() {
        {   //0,9,0
            From = new DateTime(2018,6,2,0,0);
            Amount = 9;
            LengthCount = 1;
            LengthType = DayType.Day;
            Category = "1d_n";
        }
    };
    private static final Row Test26_2Day_None = new Row() {
        {   //0,13,13,0
            From = new DateTime(2018,6,3,0,0);
            Amount = 26;
            LengthCount = 2;
            LengthType = DayType.Day;
            Category = "2d_n";
        }
    };
    private static final Row Test14_2Day_2Day = new Row() {
        {//0,7,7,7,7,7,7,0
            From = new DateTime(2018,6,6,0,0);
            Amount = 14;
            LengthCount = 2;
            LengthType = DayType.Day;
            RepeatCount = 2;
            RepeatType = DayType.Day;
            RepeatEnd = new DateTime(2018,6,10,0,0);
            Category = "2d_2d";
        }
    };
    private static final Row Test730_Year_Year = new Row() {
        {   //0,2,2,2...
            From = new DateTime(2018,6,1,0,0);
            Amount = 730;
            LengthCount = 1;
            LengthType = DayType.Year;
            RepeatCount = 1;
            RepeatType = DayType.Year;
            Category = "1y_1y";
            IsIncome = true;
        }
    };
    //endregion

    @Before
    public void initTests() {
        DateTimeZone.setProvider(new UTCProvider());
    }

    @Test
    public void SimpleDay() {
        Row row = Test9_1Day_None;
        String result = row.Validate();
        assertNotNull(result, "Row validation error: " + result);

        assertEquals(row.CalcPerDay(), -9f, DELTA);
        assertEquals(row.CalcFirstPeriodEndDay(), row.From.plusDays(1)); //because we ignore the last day of the period this makes sense
    }

    @Test
    public void SimpleYear() {
        Row row = Test730_Year_Year;
        String result = row.Validate();
        assertNotNull(result, "Row validation error: " + result);

        assertEquals(row.CalcPerDay(), 2f, DELTA);
        assertEquals(row.CalcFirstPeriodEndDay(), new DateTime(2019,6,1, 0, 0));
    }

    @Test
    public void Large()
    {
        Calc calc = new Calc(Arrays.asList(Test9_1Day_None,
                Test26_2Day_None,
                Test14_2Day_2Day,
                Test730_Year_Year));

        //assert some things
        assertTrue("A category is missing", calc.GetCategories().contains("1d_n"));

        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,5,31,0,0))), 0f, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,1,0,0))), 2f, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,2,0,0))), 2f - 9, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,3,0,0))), 2f - 13, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,4,0,0))), 2f - 13, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,5,0,0))), 2f, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,6,0,0))), 2f - 7, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,7,0,0))), 2f - 7, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,8,0,0))), 2f - 7, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,9,0,0))), 2f - 7, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,10,0,0))), 2f - 7, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,11,0,0))), 2f - 7, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,12,0,0))), 2f, DELTA);

        //week
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Week, new DateTime(2018,6,3,0,0))), 2f*3 - 9 - 13, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Week, new DateTime(2018,6,4,0,0))), 2f*7 - 13 - 14/2*5, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Week, new DateTime(2018,6,12,0,0))), 2f*7 - 7, DELTA); //includes 2018/06/11

        //month
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Month, new DateTime(2018,6,1,0,0))), 2f*30 - 13*2 - 9 - 14/2*6, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Month, new DateTime(2018,7,1,0,0))), 2f*31, DELTA);

        //TODO more tests when it figures out the range (1d_2w) problem
    }


    @Test
    public void Income() {
        Calc calc = new Calc(Arrays.asList(
            new Row()
            {
                {
                    From = new DateTime(2018,5,16,0,0);
                    Amount = 1000;
                    LengthCount = 4;
                    LengthType = DayType.Week;
                    Category = "income?";
                    IsIncome = true;
                    RepeatCount = 4;
                    RepeatType = DayType.Week;
                    RepeatEnd = new DateTime(2019,1,1,0,0);
                }
            },
            new Row()
            {
                {
                    From = new DateTime(2018,5,26,0,0);
                    Amount = 100;
                    LengthCount = 4;
                    LengthType = DayType.Week;
                    Category = "food";
                    IsIncome = false;
                    RepeatCount = 4;
                    RepeatType = DayType.Week;
                    RepeatEnd = new DateTime(2019,1,1,0,0);
                }
            })
        );

        float incomeDay = 1000 / 28f;
        float foodDay = 100 / 28f;

        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,1,0,0))), incomeDay - foodDay, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,6,30,0,0))), incomeDay - foodDay, DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Day, new DateTime(2018,7,1,0,0))), incomeDay - foodDay, DELTA);

        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Week, new DateTime(2018,6,1,0,0))), 7 * (incomeDay - foodDay), DELTA);
        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Week, new DateTime(2018,7,1,0,0))), 7 * (incomeDay - foodDay), DELTA);

        assertEquals(calc.TotalFor(new DayTypePeriod(DayType.Month, new DateTime(2018,7,1,0,0))), 31 * (incomeDay - foodDay), DELTA*10); //31 days in july (with more error)
        //TODO fix this delta error
    }
}
