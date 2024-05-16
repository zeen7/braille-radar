package com.example.brailleradar;

import com.example.brailleradar.models.TagInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentsSingleton {
    private static final List<TagInfo> recentTagList = Collections.synchronizedList(new ArrayList<>());

    public static List<TagInfo> getRecentTagList() {
        return recentTagList;
    }
    public static void setRecentTagList(TagInfo tag){
        recentTagList.add(0, tag);
        if(recentTagList.size() == 10){
            recentTagList.remove(recentTagList.size()-1);
        }
    }
}
