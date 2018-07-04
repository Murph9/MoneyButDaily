package com.murph9.moneybutdaily;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.murph9.moneybutdaily.database.RowRepository;
import com.murph9.moneybutdaily.model.Row;

import java.util.List;

public class RowViewModel extends AndroidViewModel {
    private RowRepository mRepository;
    private LiveData<List<Row>> mAllRows;

    public RowViewModel (Application app) {
        super(app);
        mRepository = new RowRepository(app);
        mAllRows = mRepository.getAllRows();
    }

    public LiveData<List<Row>> getAllRows() { return mAllRows; }

    public void insert(Row row) { mRepository.insert(row); }
}
