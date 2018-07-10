package com.murph9.moneybutdaily.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.murph9.moneybutdaily.model.Row;

import java.util.List;
import java.util.concurrent.ExecutionException;

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

    public Row get(int lineId) {
        try {
            return new GetRowAsyncTask(mRowDao).execute(lineId).get(); //TODO not happy about this call
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static class GetRowAsyncTask extends AsyncTask<Integer, Void, Row> {
        private RowDao mAsyncTaskDao;
        public GetRowAsyncTask(RowDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Row doInBackground(Integer... integers) {
            return mAsyncTaskDao.getSingle(integers[0]);
        }
    }

    public void insert(Row row) {
        new VoidRowAsyncTask(mRowDao, VoidRowAsyncTask.Task.Insert).execute(row);
    }
    public void update(Row row) {
        new VoidRowAsyncTask(mRowDao, VoidRowAsyncTask.Task.Update).execute(row);
    }
    public void delete(Row row) {
        new VoidRowAsyncTask(mRowDao, VoidRowAsyncTask.Task.Delete).execute(row);
    }

    private static class VoidRowAsyncTask extends AsyncTask<Row, Void, Void> {
        enum Task {
            Insert,
            Update,
            Delete;
        }

        private Task task;
        private RowDao mAsyncTaskDao;

        VoidRowAsyncTask(RowDao dao, Task task) {
            mAsyncTaskDao = dao;
            this.task = task;
        }

        @Override
        protected Void doInBackground(final Row... params) {
            switch (task) {
                case Insert:
                    mAsyncTaskDao.insert(params[0]);
                    break;
                case Update:
                    mAsyncTaskDao.update(params[0]);
                    break;
                case Delete:
                    mAsyncTaskDao.delete(params[0]);
                    break;
            }

            return null;
        }
    }
}
