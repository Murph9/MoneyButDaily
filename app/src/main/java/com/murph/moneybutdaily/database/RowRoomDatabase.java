package com.murph.moneybutdaily.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import com.murph.moneybutdaily.model.Row;

@Database(entities = {Row.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class RowRoomDatabase extends RoomDatabase {
    public abstract RowDao rowDao();

    private static RowRoomDatabase INSTANCE;

    static RowRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RowRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), RowRoomDatabase.class, "money_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
        new RoomDatabase.Callback() {
            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
            }

            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                //on database create, do anything that is required using a class like:
                // new ###(INSTANCE).execute();
                // private static class ### extends AsyncTask<Void, Void, Void>
            }
        };
}
