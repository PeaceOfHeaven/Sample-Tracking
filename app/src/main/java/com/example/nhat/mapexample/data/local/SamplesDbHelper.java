package com.example.nhat.mapexample.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nhat.mapexample.data.local.SamplesPersistenceContract.SampleEntry;

/**
 * Created by Nhat on 10/24/2016.
 */

public class SamplesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Samples.db";

    private static final int DATABASE_VERSION = 1;

    public SamplesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ").append(SampleEntry.TABLE_NAME)
                .append("(")
                .append(SampleEntry._ID).append(" INTEGER PRIMARY KEY,")
                .append(SampleEntry.COLUMN_NAME_SAMPLE_ID_COLUMN).append(" INTEGER NOT NULL,")
                .append(SampleEntry.COLUMN_NAME_NAME_COLUMN).append(" TEXT NOT NULL,")
                .append(SampleEntry.COLUMN_NAME_ADDRESS_COLUMN).append(" TEXT,")
                .append(SampleEntry.COLUMN_NAME_LAT_COLUMN).append(" TEXT NOT NULL,")
                .append(SampleEntry.COLUMN_NAME_LONG_COLUMN).append(" TEXT NOT NULL,")
                .append(SampleEntry.COLUMN_NAME_DATE_COLUMN).append(" TEXT NOT NULL,")
                .append(SampleEntry.COLUMN_NAME_SYNC_COLUMN).append(" INTEGER NOT NULL,")
                .append("UNIQUE (sample_id) ON CONFLICT IGNORE")
                .append(");");
        db.execSQL(builder.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SampleEntry.TABLE_NAME);
        onCreate(db);
    }
}
