package com.example.onlinetrivia;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalMessageListener {
    private static final GlobalMessageListener instance = new GlobalMessageListener();

    private final List<String> messages = new ArrayList<>();
    private final List<String> privateMessages = new ArrayList<>();
    private final List<String> namesList = new ArrayList<>();

    private GlobalMessageListener() {
    }

    public static GlobalMessageListener getInstance() {
        return instance;
    }

    public synchronized void addMessage(String channel, String message) {
        messages.add(channel + ": " + message);
    }

    public synchronized void addPrivateMessage(String sender, String recipient, String message) {
        privateMessages.add(sender + "->" + recipient + ": " + message);
    }

    public synchronized List<String> getMessagesFor(String channel) {
        return messages.stream().filter(msg -> msg.startsWith(channel + ":")).collect(Collectors.toList());
    }

    public synchronized List<String> getPrivateMessagesFor(String recipient) {
        return privateMessages.stream().filter(msg -> msg.contains("->" + recipient + ":")).collect(Collectors.toList());
    }

    public synchronized void addUserToNamesList(String name) {
        namesList.add(name);
    }

    public synchronized void removeUserFromNamesList(String name) {
        namesList.remove(name);
    }

    public synchronized List<String> getNamesList() {
        return new ArrayList<>(namesList);
    }
}
