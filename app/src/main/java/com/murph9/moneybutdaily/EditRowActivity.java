package com.murph9.moneybutdaily;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;

import org.joda.time.DateTime;


public class EditRowActivity extends AppCompatActivity {

    public static final String EXTRA_LINE = "com.murph.moneybutdaily.Line";
    public static final String DATE_FORMAT = "yyyy/MM/dd";

    private EditText mEditAmountView;
    private EditText mEditLengthCountView;
    private Spinner mEditLengthTypeView;
    private EditText mEditCategoryView;
    private CheckBox mEditIsIncomeView;
    private CheckBox mEditIsRepeatView;

    private DateTime From = new DateTime();
    private DateTime RepeatEnd = new DateTime();

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

        mEditLengthTypeView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DayType.CAN_SELECT));

        //read in the given fields (if any)
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            int lineId = intent.getIntExtra(EXTRA_LINE, 0);

            editRow = EditRowActivity.this.rowViewModel.get(lineId);

            mEditAmountView.setText(editRow.Amount+"");
            mEditLengthCountView.setText(editRow.LengthCount+"");
            mEditLengthTypeView.setSelection(DayType.CAN_SELECT.indexOf(editRow.LengthType));
            mEditCategoryView.setText(editRow.Category);
            mEditIsIncomeView.setChecked(editRow.IsIncome);
            mEditIsRepeatView.setChecked(editRow.RepeatType != DayType.None);

            From = editRow.From;
            TextView startDate = findViewById(R.id.startDateValue);
            startDate.setText(From.toString(EditRowActivity.DATE_FORMAT));

            if (editRow.RepeatEnd != null) {
                RepeatEnd = editRow.RepeatEnd;
                TextView repeatDate = findViewById(R.id.repeatDateValue);
                repeatDate.setText(RepeatEnd.toString(EditRowActivity.DATE_FORMAT));
            }

        } else {
            //remove the delete button, as its not usable on create
            Button deleteButton = findViewById(R.id.button_delete);
            deleteButton.setVisibility(View.GONE);
        }

        //programmatically setting a button action
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
                    row.RepeatType = row.LengthType; //we are not supporting different lengths here
                    row.RepeatEnd = EditRowActivity.this.RepeatEnd;
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

    public void onDeleteClick(View view) {
        //are you sure dialog?

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        EditRowActivity.this.rowViewModel.delete(editRow);
                        setResult(RESULT_CANCELED);
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }

    public void onFromDateClick(View view) {
        DatePickerFragment frag = new DatePickerFragment();
        frag.setCallback(new DatePickerCallback() {
            @Override
            public void setFields(DateTime date) {
                EditRowActivity.this.From = date;
                TextView startDate = EditRowActivity.this.findViewById(R.id.startDateValue);
                startDate.setText(EditRowActivity.this.From.toString(EditRowActivity.DATE_FORMAT));
            }
        });

        Bundle args = new Bundle(1);
        args.putLong(EditRowActivity.DatePickerFragment.EXTRA_INPUT, From.getMillis());
        frag.setArguments(args);

        frag.show(getFragmentManager(), "datePicker");
    }

    public void onRepeatDateClick(View view) {
        DatePickerFragment frag = new DatePickerFragment();
        frag.setCallback(new DatePickerCallback() {
            @Override
            public void setFields(DateTime date) {
                EditRowActivity.this.RepeatEnd = date;
                TextView repeatDate = EditRowActivity.this.findViewById(R.id.repeatDateValue);
                repeatDate.setText(EditRowActivity.this.RepeatEnd.toString(EditRowActivity.DATE_FORMAT));
            }
        });

        Bundle args = new Bundle(1);
        args.putLong(EditRowActivity.DatePickerFragment.EXTRA_INPUT, RepeatEnd.getMillis());
        frag.setArguments(args);

        frag.show(getFragmentManager(), "datePicker");
    }

    public interface DatePickerCallback {
        void setFields(DateTime date);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        public static final String EXTRA_INPUT = "com.murph.moneybutdaily.DatePickerFragment.INPUT";

        private DateTime inputDate;

        private DatePickerCallback callback;
        public void setCallback(DatePickerCallback callback) {
            this.callback = callback;
        }

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
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, this.inputDate.getYear(),
                    this.inputDate.getMonthOfYear() - 1, this.inputDate.getDayOfMonth());
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Run the callback with the new date
            callback.setFields(new DateTime(year,month + 1,day,0,0));
        }
    }


    public void onInfoButtonClick(View view) {
        Toast.makeText(this, "Set the first day on the last repeat this row applies on. For example the last monday for a repeating week entry.", Toast.LENGTH_LONG).show();
    }
}
