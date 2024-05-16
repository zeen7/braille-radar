package com.example.brailleradar.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class ClusterInfo implements Parcelable {
    private String clusterId;
    private String name;
    public ClusterInfo(String clusterId, String name) {
        this.clusterId = clusterId;
        this.name = name;
    }


    // Getter and setter for tagId
    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    // Getter and setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Parcelable implementation
    protected ClusterInfo(Parcel in) {
        clusterId = in.readString();
        name = in.readString();
    }

    public static final Creator<ClusterInfo> CREATOR = new Creator<ClusterInfo>() {
        @Override
        public ClusterInfo createFromParcel(Parcel in) {
            return new ClusterInfo(in);
        }

        @Override
        public ClusterInfo[] newArray(int size) {
            return new ClusterInfo[size];
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

    public String toString() {
        return name + " (" + clusterId + ")";
    }
}
