package com.murph.moneybutdaily;

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

import com.murph.moneybutdaily.model.DayType;
import com.murph.moneybutdaily.model.Row;

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

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final RowListAdapter adapter = new RowListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRowViewViewModel = ViewModelProviders.of(this).get(RowViewModel.class);
        mRowViewViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                //TODO left as an example of how to dynamically update a list
                //adapter.setRows(rows);

                setPageData(rows);
            }
        });
    }

    private void setPageData(List<Row> rows) {
        calc = new Calc(rows);

        float todayValue = calc.TotalForDay(new DateTime());
        TextView todayText = findViewById(R.id.todayText);
        todayText.setText(""+todayValue);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_ROW_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Row row = new Row();
            row.Amount = data.getFloatExtra(NewRowActivity.EXTRA_AMOUNT, -1f);
            row.From = new DateTime((long)data.getSerializableExtra(NewRowActivity.EXTRA_FROM));
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
