package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.murph9.moneybutdaily.model.Row;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private final int TAB_TODAY = 0;
    private final int TAB_WEEK = 1;
    private final int TAB_MONTH = 2;

    private int tabId = TAB_TODAY;
    private Calc calc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

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
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                setTabId(tab.getPosition());
            }
        });
    }

    private Map<String, Float> getReportData() {
        switch (this.tabId) {
            case TAB_TODAY:
                return calc.ReportForDay(new DateTime());
            case TAB_WEEK:
                return calc.ReportForWeek(new DateTime());
            case TAB_MONTH:
                return calc.ReportForMonth(new DateTime());
            default:
                Toast.makeText(this, "Invalid tab selected", Toast.LENGTH_SHORT);
                return null;
        }
    }

    private void setPageData(List<Row> rows) {
        calc = new Calc(rows);

        updateData();
    }

    private void updateData() {
        String str = "";
        Map<String, Float> report = getReportData();
        for (String key: report.keySet()) {
            str += key + ": $" + report.get(key).toString() + System.getProperty("line.separator");
        }

        TextView reportText = findViewById(R.id.report_textview);
        reportText.setText(str);
    }

    private void setTabId(int id) {
        this.tabId = id;
        updateData();
    }
}
