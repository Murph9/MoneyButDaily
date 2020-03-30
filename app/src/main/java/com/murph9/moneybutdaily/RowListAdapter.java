package com.murph9.moneybutdaily;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.murph9.moneybutdaily.model.Row;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

//basically allows a dynamically updating list of entries

public class RowListAdapter extends RecyclerView.Adapter<RowListAdapter.RowViewHolder> {

    private final String FORMAT = "yyy/MM/dd";
    private final LayoutInflater mInflater;
    private RowListActivity activity;

    private List<Row> fullRowList; // cached copy of rows
    private List<Row> mRows; // visible cached copy of rows
    private String categoryFilter; //filter which changes between the 2

    RowListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void addActivityCallback(RowListActivity rowListActivity) {
        this.activity = rowListActivity;
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
            final Row r = mRows.get(position);
            holder.rowItem_Category.setText(r.Category);
            holder.rowItem_Amount.setText("$"+r.Amount);
            holder.rowItem_StartDate.setText("@ "+ H.formatDate(r.From, FORMAT));
            holder.rowItem_LengthCount.setText(r.LengthCount+" x ");
            holder.rowItem_LengthType.setText(r.LengthType+"");
            holder.rowItem_Repeat.setText(r.Repeats ? "y" : "n");
            holder.rowItem_PerDay.setText("$"+H.to2Places(r.CalcPerDay())+"/day");

            //add listener for button press
            holder.rowItem_Edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.editRow(r);
                }
            });
        } else {
            // Covers the case of data not being ready yet.
            holder.rowItem_StartDate.setText("No Rows found");
        }
    }

    void setFilter(String filter) {
        this.categoryFilter = filter;

        filterList();
    }

    void setRows(List<Row> rows){
        fullRowList = rows;

        filterList();
    }

    private void filterList() {
        if (this.categoryFilter == null || this.categoryFilter.isEmpty())
            mRows = fullRowList;
        else {
            mRows = new LinkedList<>();
            for (Row r: this.fullRowList) {
                if (r.Category.toLowerCase().contains(this.categoryFilter.toLowerCase())) //PERF ??
                    mRows.add(r);
            }
        }

        //sort mRows by most recent
        Collections.sort(mRows, new Comparator<Row>() {
            @Override
            public int compare(Row o1, Row o2) {
                return o1.From.compareTo(o2.From)*-1; //most recent first
            }
        });

        notifyDataSetChanged();
    }

    List<Row> getRows() {
        return fullRowList;
    }

    // getItemCount() is called many times, and when it is first called,
    // mRows has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mRows != null) //TODO or fullRowList?
            return mRows.size();
        else return 0;
    }

    class RowViewHolder extends RecyclerView.ViewHolder {
        private final TextView rowItem_Category;
        private final TextView rowItem_Amount;
        private final TextView rowItem_StartDate;
        private final TextView rowItem_LengthCount;
        private final TextView rowItem_LengthType;
        private final TextView rowItem_Repeat;
        private final TextView rowItem_PerDay;
        private final Button rowItem_Edit;

        private RowViewHolder(View itemView) {
            super(itemView);
            rowItem_Category = itemView.findViewById(R.id.row_list_item_category);
            rowItem_Amount = itemView.findViewById(R.id.row_list_item_amount);
            rowItem_StartDate = itemView.findViewById(R.id.row_list_item_startdate);
            rowItem_LengthCount = itemView.findViewById(R.id.row_list_item_lengthcount);
            rowItem_LengthType = itemView.findViewById(R.id.row_list_item_lengthtype);
            rowItem_PerDay = itemView.findViewById(R.id.row_list_item_perday);
            rowItem_Repeat = itemView.findViewById(R.id.row_list_item_repeat);
            rowItem_Edit = itemView.findViewById(R.id.row_list_item_edit);
        }
    }
}