package com.example.onlinetrivia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedDataSource {

    private static final SharedDataSource instance = new SharedDataSource();

    private List<String> combinedList = new ArrayList<>();
    private String recipientName;

    private Map<String, List<String>> messagesMap = new HashMap<>();
    private String activePrivateChatUser;

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

    public void storePrivateMessage(String sender, String message) {
        String formattedMessage = sender + ": " + message;
        if (!messagesMap.containsKey(sender)) {
            messagesMap.put(sender, new ArrayList<>());
        }
        messagesMap.get(sender).add(formattedMessage);
    }



    public void updateCombinedList(List<String> updatedList) {
        for (String item : updatedList) {
            if (!combinedList.contains(item)) {
                combinedList.add(item);
            }
        }
    }

    public void handlePrivateMessage(String sender, String message) {
        // Store the private message in SharedDataSource
        SharedDataSource.getInstance().storeMessage(sender, message);
    }

    public void setActivePrivateChatUser(String user) {
        this.activePrivateChatUser = user;
    }

    public String getActivePrivateChatUser() {
        return activePrivateChatUser;
    }


    public List<String> getStoredMessages(String channelOrUser) {
        return messagesMap.getOrDefault(channelOrUser, new ArrayList<>());
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void clearStoredMessages(String channelName) {
        messagesMap.remove(channelName);
    }

}
