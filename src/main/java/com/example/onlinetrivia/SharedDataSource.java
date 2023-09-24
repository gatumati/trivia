package com.example.onlinetrivia;
import java.util.ArrayList;

import java.util.List;

public class SharedDataSource {

    private static final SharedDataSource instance = new SharedDataSource();

    private List<String> combinedList = new ArrayList<>();

    private SharedDataSource() {
        // private constructor to prevent instantiation
    }

    public static SharedDataSource getInstance() {
        return instance;
    }

    public List<String> getCombinedList() {
        return combinedList;
    }

    public void setCombinedList(List<String> list) {
        this.combinedList = list;
    }
}
