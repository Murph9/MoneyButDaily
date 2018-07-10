package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.murph9.moneybutdaily.model.Row;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int EDIT_ROW_ACTIVITY_REQUEST_CODE = 1;
    public static final int REPORT_ACTIVITY_REQUEST_CODE = 2;
    public static final int ROW_LIST_ACTIVITY_REQUEST_CODE = 3;
    public static RowViewModel RowViewViewModel;

    public Calc calc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JodaTimeAndroid.init(this);

        //init charts
        GraphView weekGraph = findViewById(R.id.week_graph);
        //note offsets
        weekGraph.getViewport().setMinX(new DateTime().minusDays(6).minusHours(12).getMillis());
        weekGraph.getViewport().setMaxX(new DateTime().plusHours(12).getMillis());
        weekGraph.getViewport().setXAxisBoundsManual(true);
        weekGraph.getViewport().setScrollable(true);

        weekGraph.getGridLabelRenderer().setHumanRounding(false); //rounding dates makes no sense
        weekGraph.getGridLabelRenderer().setLabelFormatter(new DateXAxisLabelFormatter());
        weekGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        weekGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        weekGraph.getGridLabelRenderer().setNumVerticalLabels(6);

        GraphView monthGraph = findViewById(R.id.month_graph);
        //note offsets
        monthGraph.getViewport().setMinX(new DateTime().minusWeeks(4).minusHours(12).getMillis());
        monthGraph.getViewport().setMaxX(new DateTime().plusHours(12).getMillis());
        monthGraph.getViewport().setXAxisBoundsManual(true);
        monthGraph.getViewport().setScrollable(true);

        monthGraph.getGridLabelRenderer().setHumanRounding(false); //rounding dates makes no sense
        monthGraph.getGridLabelRenderer().setLabelFormatter(new DateXAxisLabelFormatter());
        monthGraph.getGridLabelRenderer().setNumHorizontalLabels(5);
        monthGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        monthGraph.getGridLabelRenderer().setNumVerticalLabels(6);

        RowViewViewModel = ViewModelProviders.of(this).get(RowViewModel.class);
        RowViewViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                setPageData(rows);
            }
        });
    }

    private void setPageData(List<Row> rows) {
        calc = new Calc(rows);

        float todayValue = calc.TotalForDay(new DateTime());
        TextView todayText = findViewById(R.id.todayText);
        todayText.setText(""+todayValue);

        //update graph(s)
        //http://www.android-graphview.org/download-getting-started/
        GraphView weekGraph = findViewById(R.id.week_graph);
        weekGraph.removeAllSeries();

        int rowCount = 12;

        List<DataPoint> points = new LinkedList<>();
        for (int i = -rowCount; i <= 0; i++) {
            DateTime dt = new DateTime().plusDays(i);
            float totalToday = calc.TotalForDay(dt);
            points.add(new DataPoint(dt.getMillis(), totalToday));
        }
        //then add a future day so its empty
        points.add(new DataPoint(new DateTime().plusDays(1).getMillis(), 0));

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(points.toArray(new DataPoint[]{}));
        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            public int get(DataPoint data) {
                //very negative should be red (255,0,0)
                //very positive should be green (0,255,0)
                //yellow in the middle (255,255,0)
                double val = data.getY();
                if (val >= 0) {
                    return Color.rgb((int)((Math.atan(-val/30)/Math.PI*2 + 1)*255), 255, 0);
                } else {
                    return Color.rgb(255, (int)((Math.atan(val/10)/Math.PI*2 + 1)*255), 0);
                }
            }
        });
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLACK);
        series.setSpacing(10);

        weekGraph.addSeries(series);
        weekGraph.invalidate();


        GraphView monthGraph = findViewById(R.id.month_graph);
        monthGraph.removeAllSeries();

        rowCount = 8;

        points = new LinkedList<>();
        for (int i = -rowCount; i <= 0; i++) {
            DateTime dt = new DateTime().plusWeeks(i);
            float totalWeek = calc.TotalForWeek(dt);
            points.add(new DataPoint(dt.getMillis(), totalWeek));
        }
        //then add a future week so its empty
        points.add(new DataPoint(new DateTime().plusWeeks(1).getMillis(), 0));

        series = new BarGraphSeries<>(points.toArray(new DataPoint[]{}));
        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            public int get(DataPoint data) {
                //very negative should be red (255,0,0)
                //very positive should be green (0,255,0)
                //yellow in the middle (255,255,0)
                double val = data.getY();
                if (val >= 0) {
                    return Color.rgb((int)((Math.atan(-val/30)/Math.PI*2 + 1)*255), 255, 0);
                } else {
                    return Color.rgb(255, (int)((Math.atan(val/10)/Math.PI*2 + 1)*255), 0);
                }
            }
        });
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLACK);
        series.setSpacing(10);

        monthGraph.addSeries(series);
        monthGraph.invalidate();
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

    private class DateXAxisLabelFormatter extends DefaultLabelFormatter {
        private String format = "M-d";

        public DateXAxisLabelFormatter() { }

        @Override
        public String formatLabel(double value, boolean isValueX) {
            if (isValueX) {
                return new DateTime((long) value).toString(this.format);
            } else {
                return super.formatLabel(value, isValueX);
            }
        }
    }
}

//TODO
/*
 fix gaps in repeat (1 day repeat 1 week leaves 6 days gap)
 test cases for the stupid day edge cases
 a page for selecting rows for editing/removing
 export/import? rows button
*/
