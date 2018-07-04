package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int NEW_ROW_ACTIVITY_REQUEST_CODE = 1;
    private RowViewModel mRowViewViewModel;

    private Calc calc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JodaTimeAndroid.init(this);

        ///* TODO should be left as an example of how to dynamically update a view
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final RowListAdapter adapter = new RowListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //*/

        mRowViewViewModel = ViewModelProviders.of(this).get(RowViewModel.class);
        mRowViewViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                //TODO should be left as an example of how to dynamically update a view
                adapter.setRows(rows);

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
        weekGraph.getGridLabelRenderer().setHumanRounding(false); //founding dates makes no sense
        weekGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
        });
        for (int i = 0; i < 6; i++) {
            DateTime dt = new DateTime().plusDays(i - 6);
            float totalToday = calc.TotalForDay(dt);
            series.appendData(new DataPoint(dt.toDate(), totalToday), false, 10);
        }
        //TODO graph not showing anything useful - data seems to be correct
        weekGraph.addSeries(series);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_ROW_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Row row = new Row();
            row.Amount = data.getFloatExtra(NewRowActivity.EXTRA_AMOUNT, -1f);
            long fromLong = data.getLongExtra(NewRowActivity.EXTRA_FROM, Long.MAX_VALUE);
            if (fromLong != Long.MAX_VALUE)
                row.From = new DateTime(fromLong);

            row.LengthCount = data.getIntExtra(NewRowActivity.EXTRA_LENGTHCOUNT, 0);
            row.LengthType = DayType.valueOf(DayType.class, data.getSerializableExtra(NewRowActivity.EXTRA_LENGTHTYPE).toString());
            row.Category = data.getStringExtra(NewRowActivity.EXTRA_CATEGORY);
            mRowViewViewModel.insert(row);
        } else {
            Toast.makeText(this, "Not saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void addEntry(View view) {
        Intent intent = new Intent(MainActivity.this, NewRowActivity.class);
        startActivityForResult(intent, NEW_ROW_ACTIVITY_REQUEST_CODE);
    }
}

//TODO
/*
 fix gaps in repeat (1 day repeat 1 week leaves 6 days gap)
 test cases for the stupid day edge cases
 a page for selecting rows for editing/removing
 export/import? rows button
*/
