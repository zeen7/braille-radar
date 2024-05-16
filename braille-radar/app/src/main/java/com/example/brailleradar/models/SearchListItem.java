package com.example.brailleradar.models;

public class SearchListItem {
    private String tagId;
    private String tagName;
    private int iconImage;
    private String distance;
    private String otherInfo;
    public SearchListItem(String tagId, String tagName, int iconImage, String distance, String otherInfo){
        this.tagId = tagId;
        this.tagName = tagName;
        this.iconImage = iconImage;
        this.distance = distance;
        this.otherInfo = otherInfo;
    }
    public String getTagId() { return tagId; }
    public String getTagName(){
        return tagName;
    }
    public int getIconImage(){
        return iconImage;
    }
    public String getDistance(){
        return distance;
    }
    public String getOtherInfo(){
        return otherInfo;
    }

}
