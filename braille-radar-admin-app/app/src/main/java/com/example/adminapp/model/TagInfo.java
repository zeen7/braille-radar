package com.example.adminapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.databind.JsonNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private List<String> clusterNames;
    private String replacementDate;
    private Date registeredAt;
    private Date lastModified;
    private int todaysPings;
    private int lastIntervalPings;
    private int totalPings;

    // Define the date format matching the String representation
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");

    public TagInfo(JsonNode tag) {
        this.tagId = tag.get("tagId").asText();
        this.deviceName = tag.get("deviceName").asText();
        this.location = tag.get("location").isNull() ? null : tag.get("location").asText();
        this.name = tag.get("name").asText();
        this.type = tag.get("type").asText();
        this.floor = tag.get("floor").asInt();
        try {
            this.registeredAt = dateFormat.parse(tag.get("registeredAt").asText());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            this.lastModified = dateFormat.parse(tag.get("lastModified").asText());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.todaysPings = tag.get("todaysPings").asInt();
        this.lastIntervalPings = tag.get("lastIntervalPings").asInt();
        this.totalPings = tag.get("totalPings").asInt();
        coordinates = new Coordinates(tag.get("latitude").asDouble(), tag.get("longitude").asDouble());

        this.aliases = new ArrayList<>();
        for (int j = 0; j < tag.get("aliases").size(); j++) {
            this.aliases.add(tag.get("aliases").get(j).asText());
        }

        this.clusterNames = new ArrayList<>();
        JsonNode clusters = tag.get("Clusters");
        if(clusters != null) {
            for (int j = 0; j < tag.get("Clusters").size(); j++) {
                this.clusterNames.add(tag.get("Clusters").get(j).get("name").asText());
            }
        }



        this.currentDistance = -1;
    }

    public Date getRegisteredAt() {
        return registeredAt;
    }

    public Date getLastModified() {
        return lastModified;
    }
    public int getTodaysPings() {
        return todaysPings;
    }

    public int getLastIntervalPings() {
        return lastIntervalPings;
    }

    public int getTotalPings() {
        return totalPings;
    }

    public void setRegisteredAt(Date registeredAt) {
        this.registeredAt = registeredAt;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setTodaysPings(int todaysPings) {
        this.todaysPings = todaysPings;
    }

    public void setLastIntervalPings(int lastIntervalPings) {
        this.lastIntervalPings = lastIntervalPings;
    }
    public void setTotalPings(int totalPings) {
        this.totalPings = totalPings;
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

    public List<String> getClusterNames(){
        return clusterNames;
    }

    public void addClusterName(String clusterName){
        this.clusterNames.add(clusterName);
    }

    public String getReplacementDate() { return replacementDate; }
    public  void setReplacementDate(String replacementDate) {
        this.replacementDate = replacementDate;
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
        dest.writeStringList(clusterNames);
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
        clusterNames = in.createStringArrayList();
    }

    public void printTag() {
        System.out.println("Tag Information:");
        System.out.println("Tag ID: " + tagId);
        System.out.println("Device Name: " + deviceName);
        System.out.println("Location: " + (location != null ? location : "N/A"));
        System.out.println("Name: " + name);
        System.out.println("Aliases: " + aliases.toString());
        System.out.println("Type: " + type);
        System.out.println("Floor: " + floor);
        System.out.println("Coordinates: " + coordinates.toString());
        System.out.println("Current Distance: " + currentDistance);
        System.out.println("Cluster Names: " + clusterNames.toString());
        System.out.println("Registered At: " + (registeredAt != null ? dateFormat.format(registeredAt) : "N/A"));
        System.out.println("Last Modified: " + (lastModified != null ? dateFormat.format(lastModified) : "N/A"));
        System.out.println("Today's Pings: " + todaysPings);
        System.out.println("Last Interval Pings: " + lastIntervalPings);
        System.out.println("Total Pings: " + totalPings);
        System.out.println("Replacement Date: " + (replacementDate != null ? replacementDate : "N/A"));
    }

}
