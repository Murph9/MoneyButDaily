package com.murph9.moneybutdaily;

import android.arch.lifecycle.Observer;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;
import com.murph9.moneybutdaily.service.RowCsvHelper;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
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
            data.append("\n").append(RowCsvHelper.convertToCsvRow(r));
        }

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        assert clipboardManager != null;
        String clipData = data.toString();
        clipboardManager.setPrimaryClip(ClipData.newPlainText("MoneyButDaily export", "Data exported from MoneyButDaily on " + LocalDateTime.now() + " with a length of: " + clipData.length() + "\n\n" + clipData));

        //just for you jarrod
        Toast.makeText(this, "Export is now in your clipboard (prevents an sd card access permission request).", Toast.LENGTH_LONG).show();
    }

    public void importRows(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import");
        builder.setMessage("Fields in order: " + RowCsvHelper.fieldOrder());

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMaxLines(10);
        builder.setView(input);

        String inputText = null;
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean result = false;
                try {
                    result = saveRows(input.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (result) {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private boolean saveRows(String input) throws IOException {
        CSVReader reader = new CSVReader(new StringReader(input));
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            Row row = RowCsvHelper.convertFromCsvRow(nextLine);
            if (row == null) {
                Toast.makeText(this, "Line failed to save: " + reader.getLinesRead() + ", don't know anymore.", Toast.LENGTH_LONG).show();
                return false;
            }
            String result = row.Validate();
            if (result != null) {
                Toast.makeText(this, "Row: " + reader.getLinesRead() + " failed validation: " + result, Toast.LENGTH_LONG).show();
                return false;
            }
            MainActivity.RowViewViewModel.insert(row);
        }
        return true;
    }
}
