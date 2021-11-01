package com.murph9.moneybutdaily.service;

import android.text.TextUtils;

import com.murph9.moneybutdaily.database.Converters;
import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;

import java.time.LocalDateTime;
import java.util.Collection;

public class RowCsvHelper {

    public static String convertToCsvRows(Collection<Row> rows) {
        StringBuilder data = new StringBuilder();
        data.append("Amount, IsIncome, From, LengthCount, LengthType, LengthType, Category, Repeats, LastDay, Note");

        for (Row row: rows) {
            Object[] list = new Object[]{row.Amount, row.IsIncome, row.From, row.LengthCount,
                    row.LengthType, row.Category, row.Repeats, row.LastDay, row.Note};
            data.append("\n").append(TextUtils.join(", ", list));
        }
        return data.toString();
    }

    public static Row convertFromCsvRow(String[] nextLine) {
        try {
            Row r = new Row();
            r.Amount = Float.parseFloat(nextLine[0].trim());
            if (nextLine[1] != null && !nextLine[1].isEmpty())
                r.IsIncome = nextLine[1].trim().equals("true");

            r.From = LocalDateTime.parse(nextLine[2].trim());
            r.LengthCount = Integer.parseInt(nextLine[3].trim());
            r.LengthType = DayType.valueOf(nextLine[4].trim());
            r.Category = nextLine[5].trim();
            if (nextLine[6] != null && !nextLine[6].isEmpty())
                r.Repeats = nextLine[6].trim().equals("true");
            if (nextLine[7] != null && !nextLine[7].isEmpty() && !nextLine[7].trim().equals("null"))
                r.LastDay = LocalDateTime.parse(nextLine[7].trim());
            r.Note = nextLine[8].trim();
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    public static String fieldOrder() {
        return "Amount, IsIncome, From, LengthCount, LengthType, Category, Repeats, LastDay, Note";
    }
}
