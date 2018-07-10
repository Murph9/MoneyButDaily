package com.murph9.moneybutdaily;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;

import org.joda.time.DateTime;


public class EditRowActivity extends AppCompatActivity {

    public static final String EXTRA_LINE = "com.murph.moneybutdaily.Line";

    private EditText mEditAmountView;
    private EditText mEditLengthCountView;
    private Spinner mEditLengthTypeView;
    private EditText mEditCategoryView;
    private CheckBox mEditIsIncomeView;
    private CheckBox mEditIsRepeatView;
    //TODO repeat end time

    //TODO remove?
    private DateTime From = new DateTime();

    private Row editRow;
    private final RowViewModel rowViewModel;

    public EditRowActivity() {
        this.rowViewModel = MainActivity.RowViewViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_row);

        mEditAmountView = findViewById(R.id.edit_amount);
        mEditLengthCountView = findViewById(R.id.edit_lengthcount);
        mEditLengthTypeView = findViewById(R.id.edit_lengthtype);
        mEditCategoryView = findViewById(R.id.edit_category);
        mEditIsIncomeView = findViewById(R.id.is_income);
        mEditIsRepeatView = findViewById(R.id.is_repeat);

        //read in the given fields (if any)
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            int lineId = intent.getIntExtra(EXTRA_LINE, 0);

            editRow = EditRowActivity.this.rowViewModel.get(lineId);

            mEditAmountView.setText(editRow.Amount+"");
            mEditLengthCountView.setText(editRow.LengthCount+"");
            mEditLengthTypeView.setSelection(editRow.LengthType.ordinal());
            mEditCategoryView.setText(editRow.Category);
            mEditIsIncomeView.setChecked(editRow.IsIncome);
            mEditIsRepeatView.setChecked(editRow.RepeatType != DayType.None);
            From = editRow.From;
        }

        mEditLengthTypeView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DayType.CAN_SELECT));

        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Row row = new Row();
                if (editRow != null)
                    row.Line = editRow.Line;
                row.Amount = Float.parseFloat(mEditAmountView.getText().toString());
                row.From = EditRowActivity.this.From;

                row.LengthCount = Integer.parseInt(mEditLengthCountView.getText().toString());
                row.LengthType = DayType.valueOf(DayType.class, mEditLengthTypeView.getSelectedItem().toString());
                row.Category = mEditCategoryView.getText().toString();
                row.IsIncome = mEditIsIncomeView.isChecked();
                if (mEditIsRepeatView.isChecked()) {
                    row.RepeatCount = row.LengthCount;
                    row.RepeatType = row.LengthType; //not supporting different lengths here
                    //TODO repeat end
                }

                String rowError = row.Validate();
                if (rowError != null) {
                    Toast.makeText(view.getContext(), "Error in row: " + rowError, Toast.LENGTH_LONG).show();
                } else {
                    if (EditRowActivity.this.editRow != null) {
                        EditRowActivity.this.rowViewModel.update(row);
                    } else {
                        EditRowActivity.this.rowViewModel.insert(row);
                    }
                }

                setResult(RESULT_OK);
                finish();
            }
        });
    }

    public void onFromDateClick(View view) {
        DialogFragment frag = new DatePickerFragment();
        Bundle args = new Bundle(1);
        args.putLong(EditRowActivity.DatePickerFragment.EXTRA_INPUT, From.getMillis());

        frag.show(getFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        public static final String EXTRA_INPUT = "com.murph.moneybutdaily.DatePickerFragment.INPUT";

        private EditRowActivity act;
        private DateTime inputDate;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Use the input or current date as the default date
            this.inputDate = new DateTime();

            Bundle bundle = getArguments();
            if (bundle != null) {
                long millis = getArguments().getLong(EXTRA_INPUT, -1);
                if (millis != -1)
                    this.inputDate = new DateTime(millis);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            act = (EditRowActivity) getActivity();

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(act, this, this.inputDate.getYear(),
                    this.inputDate.getMonthOfYear() - 1, this.inputDate.getDayOfMonth());
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            act.From = new DateTime(year,month + 1,day,0,0);
        }
    }
}
