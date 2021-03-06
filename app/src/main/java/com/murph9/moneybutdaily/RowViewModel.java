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

    LiveData<List<Row>> getAllRows() { return mAllRows; }

    public Row get(int lineId) { return mRepository.get(lineId); }

    void insert(Row row) { mRepository.insert(row); }

    void update(Row row) { mRepository.update(row); }

    void delete(Row row) { mRepository.delete(row); }
}
