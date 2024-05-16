package com.example.adminapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Cluster implements Parcelable {
    private String clusterId;
    private String name;

    public Cluster(String clusterId, String name) {
        this.clusterId = clusterId;
        this.name = name;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    // Parcelable implementation

    protected Cluster(Parcel in) {
        clusterId = in.readString();
        name = in.readString();
    }

    public static final Creator<Cluster> CREATOR = new Creator<Cluster>() {
        @Override
        public Cluster createFromParcel(Parcel in) {
            return new Cluster(in);
        }

        @Override
        public Cluster[] newArray(int size) {
            return new Cluster[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(clusterId);
        dest.writeString(name);
    }
}
