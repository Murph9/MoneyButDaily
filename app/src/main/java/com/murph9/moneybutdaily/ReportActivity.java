package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.murph9.moneybutdaily.service.Calc;
import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.DayTypePeriod;
import com.murph9.moneybutdaily.model.Row;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    public static final String EXTRA_PERIOD = "com.murph.moneybutdaily.reportactivity.period";
    public static final String EXTRA_OFFSET = "com.murph.moneybutdaily.reportactivity.offset";

    private static final Map<Integer, DayType> tabTypeMap;
    private static final Map<DayType, Integer> typeTabMap;
    static {
        tabTypeMap = new HashMap<>();
        tabTypeMap.put(0, DayType.Day);
        tabTypeMap.put(1, DayType.Week);
        tabTypeMap.put(2, DayType.Month);
        tabTypeMap.put(3, DayType.Year);

        typeTabMap = new HashMap<>();
        typeTabMap.put(DayType.Day, 0);
        typeTabMap.put(DayType.Week, 1);
        typeTabMap.put(DayType.Month, 2);
        typeTabMap.put(DayType.Year, 3);
    }

    private int tabId;
    private int typeOffset;
    private DayType type = DayType.None;

    private Calc calc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        //init or read from intent
        typeOffset = 0;
        type = DayType.Day;
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            type = DayType.valueOf(intent.getStringExtra(EXTRA_PERIOD));
            typeOffset = intent.getIntExtra(EXTRA_OFFSET, 0);
        }
        tabId = ReportActivity.typeTabMap.get(type);

        //set the tab 'before' adding the listener
        TabLayout tabs = findViewById(R.id.report_tab_Layout);
        TabLayout.Tab tab = tabs.getTabAt(tabId);
        if (tab != null)
            tab.select(); //then set it

        RowViewModel mRowViewViewModel = ViewModelProviders.of(this).get(RowViewModel.class);
        mRowViewViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                setPageData(rows);
            }
        });


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

    private void setPageData(List<Row> rows) {
        calc = new Calc(rows);
        updatePage();
    }

    private void updatePage() {
        //update type
        switch (this.tabId) {
            case 0: //TODO hard coded ordering
                this.type = DayType.Day;
                break;
            case 1:
                this.type = DayType.Week;
                break;
            case 2:
                this.type = DayType.Month;
                break;
            case 3:
                this.type = DayType.Year;
                break;
            default:
                Toast.makeText(this, "Invalid tab selected", Toast.LENGTH_SHORT).show();
                return;
        }

        DayTypePeriod period = new DayTypePeriod(this.type, DateTime.now().withTimeAtStartOfDay()).nextPeriod(typeOffset);

        //update the value at the top
        TextView date = findViewById(R.id.text_today);
        date.setText(period.dateRangeFor());

        //update report
        TableLayout reportView = findViewById(R.id.report_table);
        reportView.removeAllViews();

        //header row
        addRow(this, reportView, "Category", "Value");
        addRow(this, reportView, "", "");

        Map<String, Float> report = calc.ReportFor(period); //TODO async
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

        typeOffset = 0; //new report type selected, reset to 0

        updatePage();
    }

    public void resetOffset(View view) {
        typeOffset = 0;
        updatePage();
    }

    public void nextDate(View view) {
        typeOffset++;
        updatePage();
    }
    public void prevDate(View view) {
        typeOffset--;
        updatePage();
    }
}
