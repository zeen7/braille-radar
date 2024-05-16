package com.example.brailleradar;

import com.example.brailleradar.models.TagInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavouritesSingleton {
    private static final List<String> favouritesTagList = Collections.synchronizedList(new ArrayList<>());

    public static List<String> getRecentTagList() {
        return favouritesTagList;
    }
    public static void setRecentTagList(String tagID){
        favouritesTagList.add(0, tagID);
    }
    public static void removeRecentTagList(String tagID){
        favouritesTagList.remove(tagID);
    }
}
