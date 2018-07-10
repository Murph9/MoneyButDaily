package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.murph9.moneybutdaily.model.Row;

import java.util.List;

public class RowListActivity extends AppCompatActivity {

    private RowViewModel mRowViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_row_list);

        // a dynamically updating view
        final RecyclerView recyclerView = findViewById(R.id.row_list);
        final RowListAdapter adapter = new RowListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRowViewModel = ViewModelProviders.of(this).get(RowViewModel.class);
        mRowViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                adapter.setRows(rows);
            }
        });
    }
}
