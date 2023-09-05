package com.example.onlinetrivia;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GlobalMessageListener {

    private static GlobalMessageListener instance;

    // To store the latest messages for each channel or user.
    private Map<String, List<String>> messageLogs = new HashMap<>();

    private GlobalMessageListener() {
    }

    public static GlobalMessageListener getInstance() {
        if (instance == null) {
            instance = new GlobalMessageListener();
        }
        return instance;
    }

    public void addMessage(String channelOrUser, String message) {
        if (!messageLogs.containsKey(channelOrUser)) {
            messageLogs.put(channelOrUser, new LinkedList<>());
        }
        messageLogs.get(channelOrUser).add(message);
    }

    public List<String> getMessagesFor(String channelOrUser) {
        return messageLogs.getOrDefault(channelOrUser, new LinkedList<>());
    }

}
