package com.example.brailleradar.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Coordinates implements Parcelable {
    public double latitude, longitude;

    public Coordinates() {
    }

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Coordinates(Coordinates coordinates) {
        latitude = coordinates.latitude;
        longitude = coordinates.longitude;
    }

    // Parcelable constructor
    private Coordinates(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    // Parcelable CREATOR
    public static final Creator<Coordinates> CREATOR = new Creator<Coordinates>() {
        @Override
        public Coordinates createFromParcel(Parcel in) {
            return new Coordinates(in);
        }

        @Override
        public Coordinates[] newArray(int size) {
            return new Coordinates[size];
        }
    };

    public String toString() {
        return "(lat: " + latitude + ", long: " + longitude + ")";
    }
}
