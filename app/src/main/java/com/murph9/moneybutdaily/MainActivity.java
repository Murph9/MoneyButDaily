package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.murph9.moneybutdaily.service.Calc;
import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.DayTypePeriod;
import com.murph9.moneybutdaily.model.Row;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

//General App TODO
/*
  test cases for the stupid day edge cases
*/

public class MainActivity extends AppCompatActivity {

    public static final int EDIT_ROW_ACTIVITY_REQUEST_CODE = 1;
    public static final int REPORT_ACTIVITY_REQUEST_CODE = 2;
    public static final int ROW_LIST_ACTIVITY_REQUEST_CODE = 3;
    public static RowViewModel RowViewViewModel;

    //TODO these are available for use as settings
    private static int COLOUR_DAY_SCALE = 60;
    private static int BAR_COUNT = 7;
    private static int BAR_FUTURE_COUNT = 1;

    private static Calc calc = new Calc(null);
    public static Calc getCalc() { return calc; }

    private Spinner mEditLengthTypeView;
    private int graphOffset;
    private DayType graphType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //My BarGraph
        final BarGraphView bgv = findViewById(R.id.bar_graph);
        bgv.init(COLOUR_DAY_SCALE); //color scale
        bgv.setOnBarTouchedListener(new BarGraphView.BarClickedListener() {
            @Override
            public void onBarClicked(int index) {
                //calc clicked period
                int offset = index + graphOffset;
                viewReportPage(offset, graphType);
            }
        });

        graphOffset = -BAR_COUNT + 1 + BAR_FUTURE_COUNT;
        graphType = DayType.Day;

        RowViewViewModel = ViewModelProviders.of(this).get(RowViewModel.class);
        RowViewViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                setPageData(rows);
            }
        });

        mEditLengthTypeView = findViewById(R.id.graph_lengthtype);
        mEditLengthTypeView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new DayType[] { DayType.Day, DayType.Week, DayType.Month, DayType.Year }));
        mEditLengthTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                graphOffset = -BAR_COUNT + 1 + BAR_FUTURE_COUNT;
                graphType = DayType.valueOf(DayType.class, mEditLengthTypeView.getSelectedItem().toString());

                bgv.setColourScale(COLOUR_DAY_SCALE * DayTypePeriod.dayCountByType(graphType));
                updateGraph();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setPageData(List<Row> rows) {
        calc = new Calc(rows);

        float todayValue = calc.TotalFor(new DayTypePeriod(DayType.Day, LocalDateTime.now()));
        TextView todayText = findViewById(R.id.todayText);
        todayText.setText(H.to2Places(todayValue));

        updateGraph();
    }

    private void updateGraph() {
        String barDateFormat = DayTypePeriod.dateFormatByType(graphType);

        BarGraphView bgv = findViewById(R.id.bar_graph);
        List<BarGraphView.Bar> bars = new LinkedList<>();
        HashMap<BarGraphView.Bar, BarGraphView.SpecialBar> barSpecials = new HashMap<>();

        LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0); //'no time'
        DayTypePeriod period = new DayTypePeriod(graphType, now);
        for (int i = 0; i < BAR_COUNT; i++) {
            DayTypePeriod curPeriod = period.nextPeriod(i + graphOffset);
            float total = calc.TotalFor(curPeriod);

            BarGraphView.Bar b = new BarGraphView.Bar(total, H.formatDate(curPeriod.date, barDateFormat));
            bars.add(b);

            //calc special labels
            if (curPeriod.contains(now)) { //same
                barSpecials.put(b, BarGraphView.SpecialBar.Current);
            } else if (curPeriod.isAfter(now)) { //after
                barSpecials.put(b, BarGraphView.SpecialBar.Future);
            }
        }
        bgv.updateBars(bars, barSpecials);
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

    public void addOffset(View view) {
        graphOffset++;
        updateGraph();
    }
    public void minusOffset(View view) {
        graphOffset--;
        updateGraph();
    }


    public void viewReportPage(int offset, DayType type) {
        Intent intent = new Intent(MainActivity.this, ReportActivity.class);
        intent.putExtra(ReportActivity.EXTRA_OFFSET, offset);
        intent.putExtra(ReportActivity.EXTRA_PERIOD, type.toString());
        startActivityForResult(intent, REPORT_ACTIVITY_REQUEST_CODE);
    }
}
