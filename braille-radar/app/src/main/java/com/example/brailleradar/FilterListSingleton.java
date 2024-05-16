package com.example.brailleradar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FilterListSingleton {
    private static final Map<Integer, String> filterList = Collections.synchronizedMap(new HashMap<>());

    public static Map<Integer, String> getInstance() {
        return filterList;
    }
}
