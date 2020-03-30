package com.murph9.moneybutdaily;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.murph9.moneybutdaily.database.Converters;
import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;

import java.time.LocalDateTime;

public class EditRowActivity extends AppCompatActivity {

    public static final String EXTRA_LINE = "com.murph.moneybutdaily.editrowactivity.line";

    private EditText mEditAmountView;
    private EditText mEditLengthCountView;
    private Spinner mEditLengthTypeView;
    private AutoCompleteTextView mEditCategoryView;
    private CheckBox mEditIsIncomeView;
    private CheckBox mEditIsRepeatView;
    private EditText mEditNotesView;

    private TextView mValuePerDay;

    private LocalDateTime From = LocalDateTime.now();
    private LocalDateTime LastDay = null;

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
        mEditLengthCountView.setText("1"); //hardcoded to start as 1
        mEditLengthTypeView = findViewById(R.id.edit_lengthtype);
        mValuePerDay = findViewById(R.id.text_per_day);

        mEditCategoryView = findViewById(R.id.edit_category);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.getCalc().GetCategories());
        mEditCategoryView.setAdapter(adapter);

        mEditIsIncomeView = findViewById(R.id.is_income);
        mEditIsRepeatView = findViewById(R.id.is_repeat);

        mEditNotesView = findViewById(R.id.edit_notes);

        mEditLengthTypeView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DayType.CAN_SELECT));

        //read in the given fields (if any)
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            int lineId = intent.getIntExtra(EXTRA_LINE, 0);

            editRow = EditRowActivity.this.rowViewModel.get(lineId);

            mEditAmountView.setText(H.to2Places(editRow.Amount));
            mEditLengthCountView.setText(editRow.LengthCount+"");
            mEditLengthTypeView.setSelection(DayType.CAN_SELECT.indexOf(editRow.LengthType));
            mEditCategoryView.setText(editRow.Category);
            mEditIsIncomeView.setChecked(editRow.IsIncome);
            mEditIsRepeatView.setChecked(editRow.Repeats);
            mEditNotesView.setText(editRow.Note);

            From = editRow.From;
            TextView startDate = findViewById(R.id.startDateValue);
            startDate.setText(H.formatDate(From, H.VIEW_YMD_FORMAT));

            if (editRow.LastDay != null) {
                LastDay = editRow.LastDay;
                TextView repeatDate = findViewById(R.id.repeatDateValue);
                repeatDate.setText(H.formatDate(LastDay, H.VIEW_YMD_FORMAT));
            }

        } else {
            //remove the delete button, as its not usable on create
            Button deleteButton = findViewById(R.id.button_delete);
            deleteButton.setVisibility(View.GONE);
        }

        //update the text box for the date
        if (From != null) {
            TextView startDate = findViewById(R.id.startDateValue);
            startDate.setText(H.formatDate(From, H.VIEW_YMD_FORMAT));
        }

        //programmatically setting a button action
        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Row row = generateRowFromView();
                if (editRow != null)
                    row.Line = editRow.Line;

                String rowError = row.Validate();
                if (rowError != null) {
                    Toast.makeText(view.getContext(), "Error in row: " + rowError, Toast.LENGTH_LONG).show();
                    return;
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

        //add a global text listener so we can update the per day value
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updatePerDay();
            }
        };
        mEditAmountView.addTextChangedListener(tw);
        mEditLengthCountView.addTextChangedListener(tw);
        mEditIsIncomeView.addTextChangedListener(tw);
        mEditLengthTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePerDay();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        mEditIsIncomeView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updatePerDay();
            }
        });

        //then lastly set focus for the amount on new
        if (editRow == null)
            mEditAmountView.requestFocus(); //TODO this doesn't open the keyboard
    }

    private Row generateRowFromView() {
        Row row = new Row();
        String amountStr = mEditAmountView.getText().toString();
        if (amountStr.isEmpty()) amountStr = "0";
        if (amountStr.startsWith(".")) amountStr = "0"+amountStr; //prevent it starting with a .
        row.Amount = Float.parseFloat(amountStr);
        row.From = H.getStartOfDay(From); //remove time information

        String lengthCountStr = mEditLengthCountView.getText().toString();
        if (lengthCountStr.isEmpty())
            lengthCountStr = "0";
        row.LengthCount = Integer.parseInt(lengthCountStr);
        row.LengthType = DayType.valueOf(DayType.class, mEditLengthTypeView.getSelectedItem().toString());
        row.Category = mEditCategoryView.getText().toString().trim();
        row.IsIncome = mEditIsIncomeView.isChecked();
        row.Repeats = mEditIsRepeatView.isChecked();
        if (mEditIsRepeatView.isChecked()) {
            //only save if repeating
            row.LastDay = EditRowActivity.this.LastDay;
        }
        row.Note = mEditNotesView.getText().toString().trim();
        return row;
    }

    private void updatePerDay() {
        String text = H.to2Places(generateRowFromView().CalcPerDay());
        mValuePerDay.setText(String.format(" $%s / day", text));
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
            public void setFields(LocalDateTime date) {
                EditRowActivity.this.From = date;
                TextView startDate = EditRowActivity.this.findViewById(R.id.startDateValue);
                startDate.setText(H.formatDate(EditRowActivity.this.From, H.VIEW_YMD_FORMAT));
            }
        });

        Bundle args = new Bundle(1);
        Long longArg = Converters.dateTimeToLong(From);
        args.putLong(EditRowActivity.DatePickerFragment.EXTRA_INPUT, longArg != null ? longArg : -1L);
        frag.setArguments(args);

        frag.show(getFragmentManager(), "datePicker");
    }

    public void onInfoButtonClick(View view) {
        Toast.makeText(this, "Set the last day this row applies on. Doesn't have to align with the repeating schedule", Toast.LENGTH_LONG).show();
    }

    public void onRepeatDateClick(View view) {
        DatePickerFragment frag = new DatePickerFragment();
        frag.setCallback(new DatePickerCallback() {
            @Override
            public void setFields(LocalDateTime date) {
                EditRowActivity.this.LastDay = date;
                TextView repeatDate = EditRowActivity.this.findViewById(R.id.repeatDateValue);
                repeatDate.setText(H.formatDate(EditRowActivity.this.LastDay, H.VIEW_YMD_FORMAT));
            }
        });

        Bundle args = new Bundle(1);
        Long longArg = Converters.dateTimeToLong(LastDay);
        args.putLong(EditRowActivity.DatePickerFragment.EXTRA_INPUT, longArg != null ? longArg : -1L);
        frag.setArguments(args);

        frag.show(getFragmentManager(), "datePicker");
    }

    public interface DatePickerCallback {
        void setFields(LocalDateTime date);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        public static final String EXTRA_INPUT = "com.murph.moneybutdaily.DatePickerFragment.INPUT";

        private LocalDateTime inputDate;

        private DatePickerCallback callback;
        public void setCallback(DatePickerCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Use the input or current date as the default date
            this.inputDate = LocalDateTime.now();

            Bundle bundle = getArguments();
            if (bundle != null) {
                long millis = getArguments().getLong(EXTRA_INPUT, -1);
                if (millis != -1)
                    this.inputDate = Converters.longToDateTime(millis);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, this.inputDate.getYear(),
                    this.inputDate.getMonth().ordinal(), this.inputDate.getDayOfMonth());
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Run the callback with the new date
            callback.setFields(LocalDateTime.of(year, month + 1, day, 0, 0));
        }
    }
}
