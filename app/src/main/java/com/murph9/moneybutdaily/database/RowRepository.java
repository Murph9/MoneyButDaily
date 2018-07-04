package com.murph9.moneybutdaily.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.murph9.moneybutdaily.model.Row;
import com.murph9.moneybutdaily.database.RowDao;
import com.murph9.moneybutdaily.database.RowRoomDatabase;

import java.util.List;

public class RowRepository {
    private RowDao mRowDao;
    private LiveData<List<Row>> mAllRows;

    public RowRepository(Application app) {
        RowRoomDatabase db = RowRoomDatabase.getDatabase(app);
        mRowDao = db.rowDao();
        mAllRows = mRowDao.getAllRows();
    }

    public LiveData<List<Row>> getAllRows() {
        return mAllRows;
    }

    public void insert(Row row) {
        new insertAsyncTask(mRowDao).execute(row);
    }

    private static class insertAsyncTask extends AsyncTask<Row, Void, Void> {

        private RowDao mAsyncTaskDao;

        insertAsyncTask(RowDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Row... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
