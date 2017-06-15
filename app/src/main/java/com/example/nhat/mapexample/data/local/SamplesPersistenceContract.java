package com.example.nhat.mapexample.data.local;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.example.nhat.mapexample.samples.domain.model.Sample;

/**
 * Created by Nhat on 11/11/2016.
 */
/**
 * The contract used for the db to save the samples locally.
 */
public final class SamplesPersistenceContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private SamplesPersistenceContract() {
    }


    /* Inner class that defines the table contents */
    public static abstract class SampleEntry implements BaseColumns {

        public static final String TABLE_NAME = "samples";
        public static final String COLUMN_NAME_SAMPLE_ID_COLUMN = "sample_id";
        public static final String COLUMN_NAME_NAME_COLUMN = "name";
        public static final String COLUMN_NAME_ADDRESS_COLUMN = "address";
        public static final String COLUMN_NAME_LAT_COLUMN = "lat";
        public static final String COLUMN_NAME_LONG_COLUMN = "long";
        public static final String COLUMN_NAME_DATE_COLUMN = "date";
        public static final String COLUMN_NAME_SYNC_COLUMN = "sync";

        public static ContentValues buildSampleContentValues(Sample sample) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_SAMPLE_ID_COLUMN, sample.getSampleId());
            values.put(COLUMN_NAME_NAME_COLUMN, sample.getName());
            values.put(COLUMN_NAME_ADDRESS_COLUMN, sample.getAddress());
            values.put(COLUMN_NAME_LAT_COLUMN, Double.toString(sample.getLatitude()));
            values.put(COLUMN_NAME_LONG_COLUMN, Double.toString(sample.getLongitude()));
            values.put(COLUMN_NAME_DATE_COLUMN, Long.toString(sample.getTime()));
            values.put(COLUMN_NAME_SYNC_COLUMN, sample.isSyncedUp() == true ? 1 : 0);
            return values;
        }
    }
}
