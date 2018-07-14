package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.murph9.moneybutdaily.model.Row;

import java.util.List;

public class RowListActivity extends AppCompatActivity {

    private RowListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_row_list);

        // a dynamically updating view
        final RecyclerView recyclerView = findViewById(R.id.row_list);
        adapter = new RowListAdapter(this);
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

    public void setFilter(View view) {
        TextView textView = findViewById(R.id.text_filter);
        adapter.setFilter(textView.getText().toString());
    }

    public void exportRows(View view) {
        StringBuilder data = new StringBuilder();
        for (Row r: adapter.getRows()) {
            data.append("\n").append(r.toExportString());
        }

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        assert clipboardManager != null;
        clipboardManager.setPrimaryClip(ClipData.newPlainText("MoneyButDaily export", data.toString()));

        //just for you jarrod
        Toast.makeText(this, "Export is now in your clipboard, this is to prevent sd card access permission).", Toast.LENGTH_LONG).show();
    }
}
