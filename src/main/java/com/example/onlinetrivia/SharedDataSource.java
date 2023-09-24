package com.example.onlinetrivia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedDataSource {

    private static final SharedDataSource instance = new SharedDataSource();

    private List<String> combinedList = new ArrayList<>();

    private Map<String, List<String>> messagesMap = new HashMap<>();

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


    public void storeMessage(String channelOrUser, String message) {
        if (!messagesMap.containsKey(channelOrUser)) {
            messagesMap.put(channelOrUser, new ArrayList<>());
        }
        messagesMap.get(channelOrUser).add(message);
    }




    public List<String> getStoredMessages(String channelOrUser) {
        return messagesMap.getOrDefault(channelOrUser, new ArrayList<>());
    }

    public void clearStoredMessages(String channelName) {
        messagesMap.remove(channelName);
    }

}
