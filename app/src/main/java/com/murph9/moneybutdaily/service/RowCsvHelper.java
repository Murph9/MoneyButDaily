package com.murph9.moneybutdaily.service;

import android.text.TextUtils;

import com.murph9.moneybutdaily.database.Converters;
import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;

public class RowCsvHelper {
    public static String convertToCsvRow(Row row) {
        Object[] list = new Object[] { row.Amount, row.IsIncome, row.From, row.LengthCount,
                row.LengthType, row.Category, row.Repeats, row.LastDay, row.Note };
        return TextUtils.join(", ", list);
    }

    public static Row convertFromCsvRow(String[] nextLine) {
        try {
            Row r = new Row();
            r.Amount = Float.parseFloat(nextLine[0]);
            if (nextLine[1] != null && !nextLine[1].isEmpty())
                r.IsIncome = nextLine[1].equals("1");

            r.From = Converters.longToDateTime(Long.parseLong(nextLine[2]));
            r.LengthCount = Integer.parseInt(nextLine[3]);
            r.LengthType = DayType.valueOf(nextLine[4]);
            r.Category = nextLine[5];
            if (nextLine[6] != null && !nextLine[6].isEmpty())
                r.Repeats = nextLine[6].equals("1");
            if (nextLine[7] != null && !nextLine[7].isEmpty())
                r.LastDay = Converters.longToDateTime(Long.parseLong(nextLine[7]));
            r.Note = nextLine[8];
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    public static String fieldOrder() {
        return "Amount, IsIncome, From, LengthCount, LengthType, Category, Repeats, LastDay, Note";
    }
}
