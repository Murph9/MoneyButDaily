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
        bgv.init(new LinkedList<BarGraphView.Bar>());

        bgv = findViewById(R.id.month_bar_graph);
        bgv.init(new LinkedList<BarGraphView.Bar>());

        RowViewViewModel = ViewModelProviders.of(this).get(RowViewModel.class);
        RowViewViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                setPageData(rows);
            }
        });

        dayGraphStart = new DateTime().minusDays(6);
        monthGraphStart = new DateTime().minusDays(new DateTime().dayOfMonth().get() - 1).minusMonths(5);
    }

    private void setPageData(List<Row> rows) {
        calc = new Calc(rows);

        float todayValue = calc.TotalForDay(new DateTime());
        TextView todayText = findViewById(R.id.todayText);
        todayText.setText(""+todayValue);

        updateGraphs();
    }

    private void updateGraphs() {
        //day graph
        String dayBarFormat = "MMM-d";

        BarGraphView bgv = findViewById(R.id.day_bar_graph);
        List<BarGraphView.Bar> bars = new LinkedList<>();

        for (int i = 0; i < 7; i++) {
            DateTime dt = dayGraphStart.plusDays(i);
            float totalToday = calc.TotalForDay(dt);
            bars.add(new BarGraphView.Bar(totalToday, dt.toString(dayBarFormat)));
        }
        bgv.updateBars(bars);

        //month graph
        String monthBarFormat = "yy-MMM";

        bgv = findViewById(R.id.month_bar_graph);
        bars = new LinkedList<>();

        for (int i = 0; i < 7; i++) {
            DateTime dt = monthGraphStart.plusMonths(i);
            float totalToday = calc.TotalForMonth(dt);
            bars.add(new BarGraphView.Bar(totalToday, dt.toString(monthBarFormat)));
        }
        bgv.updateBars(bars);
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
