package com.example.brailleradar.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class TagInfo implements Parcelable {
    private String tagId;
    private String deviceName;
    private String location;
    private String name;
    private List<String> aliases;
    private String type;
    private int floor;
    private Coordinates coordinates;
    private double currentDistance;
    private List<ClusterInfo> clusters;
    public TagInfo(JsonNode tag) {
        this.tagId = tag.get("tagId").asText();
        this.deviceName = tag.get("deviceName").asText();
        this.location = tag.get("location").isNull() ? null : tag.get("location").asText();
        this.name = tag.get("name").asText();
        this.type = tag.get("type").asText();
        this.floor = tag.get("floor").asInt();
        coordinates = new Coordinates(tag.get("latitude").asDouble(), tag.get("longitude").asDouble());

        this.aliases = new ArrayList<>();
        for (int j = 0; j < tag.get("aliases").size(); j++) {
            this.aliases.add(tag.get("aliases").get(j).asText());
        }

        this.clusters = new ArrayList<>();
        JsonNode clusters = tag.get("Clusters");
        if(clusters != null) {
            for (int j = 0; j < tag.get("Clusters").size(); j++) {
                this.clusters.add(new ClusterInfo(tag.get("Clusters").get(j).get("clusterId").asText(), tag.get("Clusters").get(j).get("name").asText()));
            }
        }

        this.currentDistance = -1;
    }


    // Getter and setter for tagId
    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    // Getter and setter for deviceName
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    // Getter and setter for location
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Getter and setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and setter for aliases
    public List getAliases() {
        return aliases;
    }

    public void setAliases(List aliases) {
        this.aliases = aliases;
    }

    // Getter and setter for type
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Getter and setter for floor
    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    // Getter and setter for coordinates
    public Coordinates getCoordinates() {
        return new Coordinates(coordinates);
    }

    public void setLatitude(Coordinates coordinates) {
        this.coordinates.latitude = coordinates.latitude;
        this.coordinates.longitude = coordinates.longitude;
    }

    public double getCurrentDistance(){
        return currentDistance;
    }

    public void setCurrentDistance(double newDistance){
        this.currentDistance = newDistance;
    }

    public List<ClusterInfo> getClusters(){
        return clusters;
    }

    public void addCluster(ClusterInfo cluster){
        this.clusters.add(cluster);
    }


    // Parcelable methods
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tagId);
        dest.writeString(deviceName);
        dest.writeString(location);
        dest.writeString(name);
        dest.writeStringList(aliases);
        dest.writeString(type);
        dest.writeInt(floor);
        dest.writeParcelable(coordinates, flags);
        dest.writeDouble(currentDistance);
        dest.writeParcelableList(clusters, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable CREATOR
    public static final Creator<TagInfo> CREATOR = new Creator<TagInfo>() {
        @Override
        public TagInfo createFromParcel(Parcel in) {
            return new TagInfo(in);
        }

        @Override
        public TagInfo[] newArray(int size) {
            return new TagInfo[size];
        }
    };

    private TagInfo(Parcel in) {
        tagId = in.readString();
        deviceName = in.readString();
        location = in.readString();
        name = in.readString();
        aliases = in.createStringArrayList();
        type = in.readString();
        floor = in.readInt();
        coordinates = in.readParcelable(Coordinates.class.getClassLoader());
        currentDistance = in.readDouble();
        clusters = in.createTypedArrayList(ClusterInfo.CREATOR);
    }

    @NonNull
    public String toString() {
        return name + " (" + tagId + ")";
    }
}
