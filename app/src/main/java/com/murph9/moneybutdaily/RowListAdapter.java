package com.murph9.moneybutdaily;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.murph9.moneybutdaily.model.DayType;
import com.murph9.moneybutdaily.model.Row;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

//basically allows a dynamically updating list of entries

public class RowListAdapter extends RecyclerView.Adapter<RowListAdapter.RowViewHolder> {

    private final DateTimeFormatter fromFormat;

    private final LayoutInflater mInflater;
    private List<Row> mRows; // Cached copy of rows

    RowListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        fromFormat = DateTimeFormat.forPattern("yyy/MM/dd");
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.layout_row_list_item, parent, false);
        return new RowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        if (mRows != null) {
            Row r = mRows.get(position);
            holder.rowItem_Category.setText(r.Category);
            holder.rowItem_Amount.setText("$"+r.Amount);
            holder.rowItem_StartDate.setText(r.From.toString(fromFormat));
            holder.rowItem_LengthCount.setText(r.LengthCount+"");
            holder.rowItem_LengthType.setText(r.LengthType+"");
            holder.rowItem_IsIncome.setText(r.IsIncome ? "y" : "n");
            holder.rowItem_Repeat.setText(r.RepeatType != DayType.None ? "y" : "n");
        } else {
            // Covers the case of data not being ready yet.
            holder.rowItem_StartDate.setText("No Row found");
        }

        //add listener for button press
        holder.rowItem_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO open intent with variable r.Line probably
            }
        });
    }

    void setRows(List<Row> rows){
        mRows = rows;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mRows has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mRows != null)
            return mRows.size();
        else return 0;
    }


    class RowViewHolder extends RecyclerView.ViewHolder {
        private final TextView rowItem_Category;
        private final TextView rowItem_Amount;
        private final TextView rowItem_StartDate;
        private final TextView rowItem_LengthCount;
        private final TextView rowItem_LengthType;
        private final TextView rowItem_IsIncome;
        private final TextView rowItem_Repeat;
        private final Button rowItem_Edit;

        private RowViewHolder(View itemView) {
            super(itemView);
            rowItem_Category = itemView.findViewById(R.id.row_list_item_category);
            rowItem_Amount = itemView.findViewById(R.id.row_list_item_amount);
            rowItem_StartDate = itemView.findViewById(R.id.row_list_item_startdate);
            rowItem_LengthCount = itemView.findViewById(R.id.row_list_item_lengthcount);
            rowItem_LengthType = itemView.findViewById(R.id.row_list_item_lengthtype);
            rowItem_IsIncome = itemView.findViewById(R.id.row_list_item_isincome);
            rowItem_Repeat = itemView.findViewById(R.id.row_list_item_repeat);
            rowItem_Edit = itemView.findViewById(R.id.row_list_item_edit);
        }
    }
}