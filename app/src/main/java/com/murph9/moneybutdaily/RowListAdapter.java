package com.murph9.moneybutdaily;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.murph9.moneybutdaily.model.Row;

import java.util.List;

//TODO
//this class is here for future use,
//basically allows a dynamically updating list

public class RowListAdapter extends RecyclerView.Adapter<RowListAdapter.RowViewHolder> {

    class RowViewHolder extends RecyclerView.ViewHolder {
        private final TextView rowItemView;

        private RowViewHolder(View itemView) {
            super(itemView);
            rowItemView = itemView.findViewById(R.id.textView);
        }
    }

    private final LayoutInflater mInflater;
    private List<Row> mRows; // Cached copy of rows

    RowListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new RowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RowViewHolder holder, int position) {
        if (mRows != null) {
            Row r = mRows.get(position);
            holder.rowItemView.setText(r.Category + " " + r.Amount + " " + r.From + " " + r.LengthType+ "*" + r.LengthCount);
        } else {
            // Covers the case of data not being ready yet.
            holder.rowItemView.setText("No Row");
        }
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
}
