package com.example.brailleradar;

import com.example.brailleradar.models.TagInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagListSingleton {
    private static final List<TagInfo> tagList = Collections.synchronizedList(new ArrayList<>());

    public static List<TagInfo> getTagList() {
        return tagList;
    }
    public static void setTagList(List <TagInfo> inputList){
        tagList.clear();
        for (TagInfo element : inputList){
            tagList.add(element);
        }
    }
}
