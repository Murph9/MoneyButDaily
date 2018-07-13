package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.murph9.moneybutdaily.model.Row;

import net.danlew.android.joda.JodaTimeAndroid;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

//TODO
/*
 fix gaps in repeat (1 day repeat 1 day leaves 6 days gap)
 test cases for the stupid day edge cases
 a page for selecting rows for editing/removing
 export/import? rows button
*/

public class MainActivity extends AppCompatActivity {

    public static final int EDIT_ROW_ACTIVITY_REQUEST_CODE = 1;
    public static final int REPORT_ACTIVITY_REQUEST_CODE = 2;
    public static final int ROW_LIST_ACTIVITY_REQUEST_CODE = 3;
    public static RowViewModel RowViewViewModel;

    public Calc calc;
    private DateTime dayGraphStart;
    private DateTime monthGraphStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JodaTimeAndroid.init(this);

        //My BarGraphs
        BarGraphView bgv = findViewById(R.id.day_bar_graph);
        bgv.init(30); //color scale TODO based on 'goal' whenever that exists

        bgv = findViewById(R.id.month_bar_graph);
        bgv.init(30*31); //color scale TODO 31?

        RowViewViewModel = ViewModelProviders.of(this).get(RowViewModel.class);
        RowViewViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                setPageData(rows);
            }
        });

        dayGraphStart = new DateTime().minusDays(5).withTimeAtStartOfDay();
        monthGraphStart = new DateTime().minusDays(new DateTime().dayOfMonth().get() - 1).minusMonths(4).withTimeAtStartOfDay();
    }

    private void setPageData(List<Row> rows) {
        calc = new Calc(rows);

        float todayValue = calc.TotalForDay(new DateTime());
        TextView todayText = findViewById(R.id.todayText);
        todayText.setText(""+todayValue);

        updateGraphs();
    }

    private void updateGraphs() {
        DateTime today = new DateTime().withTimeAtStartOfDay();

        //day graph
        String dayBarFormat = "MMM-d";

        BarGraphView weekBgv = findViewById(R.id.day_bar_graph);
        List<BarGraphView.Bar> weekBars = new LinkedList<>();
        HashMap<BarGraphView.Bar, BarGraphView.SpecialBar> weekSpecials = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            DateTime dt = dayGraphStart.plusDays(i);
            float totalToday = calc.TotalForDay(dt);
            BarGraphView.Bar b = new BarGraphView.Bar(totalToday, dt.toString(dayBarFormat));
            weekBars.add(b);

            if (dt.compareTo(today) == 0) { //today
                weekSpecials.put(b, BarGraphView.SpecialBar.Current);
            } else if (dt.compareTo(today) > 0) { //after today
                weekSpecials.put(b, BarGraphView.SpecialBar.Future);
            }
        }
        weekBgv.updateBars(weekBars, weekSpecials);

        //month graph
        String monthBarFormat = "yy-MMM";

        BarGraphView monthBgv = findViewById(R.id.month_bar_graph);
        List<BarGraphView.Bar> monthBars = new LinkedList<>();
        HashMap<BarGraphView.Bar, BarGraphView.SpecialBar> monthSpecials = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            DateTime dt = monthGraphStart.plusMonths(i);
            float totalMonth = calc.TotalForMonth(dt);

            BarGraphView.Bar b = new BarGraphView.Bar(totalMonth, dt.toString(monthBarFormat));
            monthBars.add(b);

            //calc special
            int month = dt.getMonthOfYear();
            int year = dt.getYear();
            if (month == today.getMonthOfYear() && year == today.getYear()) { //same month
                monthSpecials.put(b, BarGraphView.SpecialBar.Current);
            } else if (month > today.getMonthOfYear() && year >= today.getYear()) { //after this month
                monthSpecials.put(b, BarGraphView.SpecialBar.Future);
            }
        }
        monthBgv.updateBars(monthBars, monthSpecials);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_ROW_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Not saved", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void addEntry(View view) {
        Intent intent = new Intent(MainActivity.this, EditRowActivity.class);
        startActivityForResult(intent, EDIT_ROW_ACTIVITY_REQUEST_CODE);
    }

    public void viewReports(View view) {
        Intent intent = new Intent(MainActivity.this, ReportActivity.class);
        startActivityForResult(intent, REPORT_ACTIVITY_REQUEST_CODE);
    }

    public void viewRowList(View view) {
        Intent intent = new Intent(MainActivity.this, RowListActivity.class);
        startActivityForResult(intent, ROW_LIST_ACTIVITY_REQUEST_CODE);
    }

    public void addDay(View view) {
        dayGraphStart = dayGraphStart.plusDays(1);
        updateGraphs();
    }
    public void minusDay(View view) {
        dayGraphStart = dayGraphStart.minusDays(1);
        updateGraphs();
    }
    public void addMonth(View view) {
        monthGraphStart = monthGraphStart.plusMonths(1);
        updateGraphs();
    }
    public void minusMonth(View view) {
        monthGraphStart = monthGraphStart.minusMonths(1);
        updateGraphs();
    }
}
