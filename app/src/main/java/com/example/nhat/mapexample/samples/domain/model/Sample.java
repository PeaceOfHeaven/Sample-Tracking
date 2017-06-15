package com.example.nhat.mapexample.samples.domain.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.repackaged.com.google.common.base.Objects;

/**
 * Created by Nhat on 10/24/2016.
 */

public class Sample implements Parcelable {

    private int mId;
    private String mSampleId;
    private String mName;
    private String mResult;
    private double mLatitude;
    private double mLongitude;
    private String mAddress;
    private long mTime;
    private boolean mSyncedUp;

    public Sample() {

    }

    public int getId() {
        return mId;
    }

    public String getSampleId() {
        return mSampleId;
    }

    public void setSampleId(String sampleId) {
        this.mSampleId = sampleId;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getResult() {
        return mResult;
    }

    public void setResult(String result) {
        mResult = result;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long date) {
        mTime = date;
    }

    public boolean isSyncedUp() {
        return mSyncedUp;
    }

    public void setSyncedUp(boolean syncedUp) {
        this.mSyncedUp = syncedUp;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mSampleId, mTime, mName);
    }

    @Override
    public String toString() {
        return "Sample is collected by " + mName;
    }

    public static Sample from(Cursor cursor) {
        Sample sample = new Sample();
        sample.mId =  cursor.getInt(0);
        sample.setSampleId(cursor.getString(1));
        sample.setName(cursor.getString(2));
        sample.setAddress(cursor.getString(3));
        sample.setLatitude(cursor.getDouble(4));
        sample.setLongitude(cursor.getDouble(5));
        sample.setTime(cursor.getLong(6));
        sample.setSyncedUp(cursor.getInt(7) == 1 ? true : false);
        return sample;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mId);
        out.writeString(mSampleId);
        out.writeString(mName);
        out.writeString(mAddress);
        out.writeDouble(mLatitude);
        out.writeDouble(mLongitude);
        out.writeLong(mTime);
        out.writeBooleanArray(new boolean[]{mSyncedUp});
    }

    public static final Parcelable.Creator<Sample> CREATOR
            = new Parcelable.Creator<Sample>() {
        public Sample createFromParcel(Parcel in) {
            return new Sample(in);
        }

        public Sample[] newArray(int size) {
            return new Sample[size];
        }
    };

    private Sample(Parcel in) {
        mId = in.readInt();
        mSampleId = in.readString();
        mName = in.readString();
        mAddress = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mTime = in.readLong();

        boolean[] synData = new boolean[1];
        in.readBooleanArray(synData);
        mSyncedUp = synData[0];
    }
}
