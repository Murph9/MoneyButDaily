package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.DayTypePeriod;
import com.murph9.moneybutdaily.model.Row;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private final int TAB_DAY = 0;
    private final int TAB_WEEK = 1;
    private final int TAB_MONTH = 2;
    private final int TAB_YEAR = 3;

    private int tabId = TAB_DAY;
    private DateTime date;
    private DayType type = DayType.None;

    private Calc calc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        //init to today
        //TODO prevent future?
        date = new DateTime();
        type = DayType.Day;

        RowViewModel mRowViewViewModel = ViewModelProviders.of(this).get(RowViewModel.class);
        mRowViewViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                setPageData(rows);
            }
        });

        TabLayout tabs = findViewById(R.id.report_tab_Layout);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabId(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                setTabId(tab.getPosition());
            }
        });
    }

    private DateTime getNextDate(DateTime date) {
        return getNewDate(date, 1);
    }
    private DateTime getPrevDate(DateTime date) {
        return getNewDate(date, -1);
    }
    private DateTime getNewDate(DateTime date, int mod) {
        switch (this.tabId) {
            case TAB_DAY:
                return date.plusDays(mod);
            case TAB_WEEK:
                return date.plusWeeks(mod);
            case TAB_MONTH:
                return date.plusMonths(mod);
            case TAB_YEAR:
                return date.plusYears(mod);
            default:
                Toast.makeText(this, "Invalid tab selected", Toast.LENGTH_SHORT).show();
                return date;
        }
    }

    private Map<String, Float> getReportData() {
        switch (this.tabId) {
            case TAB_DAY:
                return calc.ReportForDay(this.date);
            case TAB_WEEK:
                return calc.ReportForWeek(this.date);
            case TAB_MONTH:
                return calc.ReportForMonth(this.date);
            case TAB_YEAR:
                return calc.ReportForYear(this.date);
            default:
                Toast.makeText(this, "Invalid tab selected", Toast.LENGTH_SHORT).show();
                return null;
        }
    }

    private void setPageData(List<Row> rows) {
        calc = new Calc(rows);
        updatePage();
    }

    private void updatePage() {
        //update type
        switch (this.tabId) {
            case TAB_DAY:
                this.type = DayType.Day;
                break;
            case TAB_WEEK:
                this.type = DayType.Week;
                break;
            case TAB_MONTH:
                this.type = DayType.Month;
                break;
            case TAB_YEAR:
                this.type = DayType.Year;
                break;
            default:
                Toast.makeText(this, "Invalid tab selected", Toast.LENGTH_SHORT).show();
                return;
        }

        //update the value at the top
        TextView date = findViewById(R.id.text_today);
        date.setText(new DayTypePeriod(this.type, this.date).dateRangeFor());

        //update report
        TableLayout reportView = findViewById(R.id.report_table);
        reportView.removeAllViews();

        //header row
        addRow(this, reportView, "Category", "Value");
        addRow(this, reportView, "", "");

        Map<String, Float> report = getReportData();
        float incomeTotal = 0;
        if (report != null) {
            //first only the income rows
            for (Map.Entry<String, Float> entry : H.entriesSortedByValue(report, true)) {
                if (entry.getValue() < 0)
                    continue;

                incomeTotal += entry.getValue();
                addRow(this, reportView, entry.getKey(), H.to2Places(entry.getValue()));
            }
        }

        addRow(this, reportView, "______", "______");
        addRow(this, reportView, "Total", H.to2Places(incomeTotal));
        addRow(this, reportView, "", "");
        float expensesTotal = 0;
        if (report != null) {
            //then next the expenses
            for (Map.Entry<String, Float> entry : H.entriesSortedByValue(report, false)) {
                if (entry.getValue() >= 0)
                    continue;
                expensesTotal += entry.getValue();
                addRow(this, reportView, entry.getKey(), H.to2Places(entry.getValue()));
            }
        }

        addRow(this, reportView, "______", "______");
        addRow(this, reportView, "Total", H.to2Places(expensesTotal));
        addRow(this, reportView, "", "");

        addRow(this, reportView, "Full Total", H.to2Places(expensesTotal + incomeTotal) +"");
    }

    private void addRow(Context context, TableLayout tl, String cat, String value) {
        TableRow tr = new TableRow(context);

        TextView view_cat = new TextView(context);
        view_cat.setText(cat);
        tr.addView(view_cat);

        TextView view_value = new TextView(context);
        view_value.setText(value);
        tr.addView(view_value);
        //TODO align text right

        tl.addView(tr);
    }

    private void setTabId(int id) {
        this.tabId = id;

        //new report type selected, reset to today
        this.date = new DateTime();

        updatePage();
    }


    public void nextDate(View view) {
        this.date = getNextDate(this.date);
        updatePage();
    }
    public void prevDate(View view) {
        this.date = getPrevDate(this.date);
        updatePage();
    }
}
