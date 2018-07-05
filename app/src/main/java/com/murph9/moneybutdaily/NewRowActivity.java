package com.murph9.moneybutdaily;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.murph9.moneybutdaily.model.DayType;

import org.joda.time.DateTime;

public class NewRowActivity extends AppCompatActivity {

    public static final String EXTRA_AMOUNT = "com.murph.moneybutdaily.AMOUNT";
    public static final String EXTRA_FROM = "com.murph.moneybutdaily.FROM";
    public static final String EXTRA_LENGTHCOUNT = "com.murph.moneybutdaily.LENGTHCOUNT";
    public static final String EXTRA_LENGTHTYPE = "com.murph.moneybutdaily.LENGTHTYPE";
    public static final String EXTRA_CATEGORY = "com.murph.moneybutdaily.CATEGORY";
    public static final String EXTRA_ISINCOME = "com.murph.moneybutdaily.ISINCOME";

    private EditText mEditAmountView;
    private EditText mEditLengthCountView;
    private Spinner mEditLengthTypeView;
    private EditText mEditCategoryView;
    private CheckBox mEditIsIncomeView;

    private DateTime From;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_row);

        mEditAmountView = findViewById(R.id.edit_amount);
        mEditLengthCountView = findViewById(R.id.edit_lengthcount);
        mEditLengthTypeView = findViewById(R.id.edit_lengthtype);
        mEditCategoryView = findViewById(R.id.edit_category);
        mEditIsIncomeView = findViewById(R.id.is_income);

        mEditLengthTypeView.setAdapter(new ArrayAdapter<DayType>(this, android.R.layout.simple_spinner_item, DayType.values()));

        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent replyIntent = new Intent();

                replyIntent.putExtra(EXTRA_AMOUNT, Float.parseFloat(mEditAmountView.getText().toString()));
                replyIntent.putExtra(EXTRA_FROM, From.getMillis());
                replyIntent.putExtra(EXTRA_LENGTHCOUNT, Integer.parseInt(mEditLengthCountView.getText().toString()));
                replyIntent.putExtra(EXTRA_LENGTHTYPE, mEditLengthTypeView.getSelectedItem().toString());
                replyIntent.putExtra(EXTRA_CATEGORY, mEditCategoryView.getText().toString());
                replyIntent.putExtra(EXTRA_ISINCOME, mEditIsIncomeView.isChecked());

                setResult(RESULT_OK, replyIntent);
                finish();
            }
        });
    }

    public void onFromDateClick(View view) {
        DialogFragment frag = new DatePickerFragment();
        frag.show(getFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private NewRowActivity act;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            act = (NewRowActivity) getActivity();

            // Use the current date as the default date in the picker
            DateTime dt = new DateTime();
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(act, this, dt.getYear(), dt.getMonthOfYear() - 1, dt.getDayOfMonth());
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            act.From = new DateTime(year,month + 1,day,0,0);
        }
    }
}
