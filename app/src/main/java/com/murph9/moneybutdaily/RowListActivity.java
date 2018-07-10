package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.murph9.moneybutdaily.model.Row;

import java.util.List;

public class RowListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_row_list);

        // a dynamically updating view
        final RecyclerView recyclerView = findViewById(R.id.row_list);
        final RowListAdapter adapter = new RowListAdapter(this);
        adapter.addActivityCallback(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MainActivity.RowViewViewModel.getAllRows().observe(this, new Observer<List<Row>>() {
            @Override
            public void onChanged(@Nullable List<Row> rows) {
                adapter.setRows(rows);
            }
        });
    }

    public void editRow(Row row) {
        Intent i = new Intent(RowListActivity.this, EditRowActivity.class);
        i.putExtra(com.murph9.moneybutdaily.EditRowActivity.EXTRA_LINE, row.Line);
        startActivity(i);
    }
}
