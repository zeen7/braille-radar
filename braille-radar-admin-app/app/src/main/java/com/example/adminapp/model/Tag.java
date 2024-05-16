package com.example.adminapp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tag {
    @JsonSerialize(using = ToStringSerializer.class)
    private String tagId;
    private String deviceName;
    private String location;
    private String name;
    private List<String> aliases;
    private String type;
    private int floor;
    private float longitude;
    private float latitude;
    private Date registeredAt;
    private Date lastModified;
    private int todaysPings;
    private int lastIntervalPings;
    private int totalPings;
    private String replacementDate;

    private List<String> clusterNames;

    // Setters
    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
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
    public  void setReplacementDate(String replacementDate) {
        this.replacementDate = replacementDate;
    }

    // Getters
    public String getTagId() {
        return tagId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getType() {
        return type;
    }

    public int getFloor() {
        return floor;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
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

    public String getReplacementDate() { return replacementDate; }


    public List<String> getClusterNames(){
        return clusterNames;
    }

    public void addClusterName(String clusterName){
        this.clusterNames.add(clusterName);
    }


    public JsonNode toJsonNode() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Serialize Tag instance to JsonNode directly
            JsonNode jsonNode = objectMapper.valueToTree(this);
            return jsonNode;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void printTag() {
        System.out.println("Tag Id: " + tagId);
        System.out.println("Device Name: " + deviceName);
        System.out.println("Location: " + location);
        System.out.println("Name: " + name);
        System.out.println("Aliases: " + aliases);
        System.out.println("Type: " + type);
        System.out.println("Floor: " + floor);
        System.out.println("Longitude: " + longitude);
        System.out.println("Latitude: " + latitude);
        System.out.println("Registered At: " + registeredAt);
        System.out.println("Last Modified: " + lastModified);
        System.out.println("Today's Pings: " + todaysPings);
        System.out.println("Last Interval Pings: " + lastIntervalPings);
        System.out.println("Total Pings: " + totalPings);
    }

    public static void printAllTags(List<Tag> tags) {
        for (Tag tag : tags) {
            System.out.println("----------- Tag -----------");
            tag.printTag();
            System.out.println("---------------------------");
        }
    }
}
