package com.murph.moneybutdaily;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;

import com.murph.moneybutdaily.model.DayType;

public class NewRowActivity extends AppCompatActivity {

    public static final String EXTRA_AMOUNT = "com.murph.moneybutdaily.AMOUNT";
    public static final String EXTRA_FROM = "com.murph.moneybutdaily.FROM";
    public static final String EXTRA_LENGTHCOUNT = "com.murph.moneybutdaily.LENGTHCOUNT";
    public static final String EXTRA_LENGTHTYPE = "com.murph.moneybutdaily.LENGTHTYPE";
    public static final String EXTRA_CATEGORY = "com.murph.moneybutdaily.CATEGORY";

    private EditText mEditAmountView;
    private CalendarView mEditFromView;
    private EditText mEditLengthCountView;
    private Spinner mEditLengthTypeView;
    private EditText mEditCategoryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_row);

        mEditAmountView = findViewById(R.id.edit_amount);
        mEditFromView = findViewById(R.id.edit_from);
        mEditLengthCountView = findViewById(R.id.edit_lengthcount);
        mEditLengthTypeView = findViewById(R.id.edit_lengthtype);
        mEditCategoryView = findViewById(R.id.edit_category);

        mEditLengthTypeView.setAdapter(new ArrayAdapter<DayType>(this, android.R.layout.simple_spinner_item, DayType.values()));

        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent replyIntent = new Intent();

                replyIntent.putExtra(EXTRA_AMOUNT, Float.parseFloat(mEditAmountView.getText().toString()));
                replyIntent.putExtra(EXTRA_FROM, mEditFromView.getDate());
                replyIntent.putExtra(EXTRA_LENGTHCOUNT, Integer.parseInt(mEditLengthCountView.getText().toString()));
                replyIntent.putExtra(EXTRA_LENGTHTYPE, mEditLengthTypeView.getSelectedItem().toString());
                replyIntent.putExtra(EXTRA_CATEGORY, mEditCategoryView.getText().toString());

                setResult(RESULT_OK, replyIntent);
                finish();
            }
        });
    }
}
