package com.example.onlinetrivia;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalMessageListener {
    private static final GlobalMessageListener instance = new GlobalMessageListener();

    private final List<String> messages = new ArrayList<>();
    private final List<String> privateMessages = new ArrayList<>();
    private final List<String> namesList = new ArrayList<>();
    private final List<String> privateChats = new ArrayList<>();
    private final List<String> channels = new ArrayList<>();
    private final List<Runnable> privateMessageListeners = new ArrayList<>();

    private GlobalMessageListener() {
    }

    public static GlobalMessageListener getInstance() {
        return instance;
    }

    public synchronized void addMessage(String channel, String message) {
        messages.add(channel + ": " + message);
        // Update the channel's message list in ChatHelper
        ChatHelper.getInstance().addMessageToChannel(channel, message);

    }

    public interface OnChatUpdateListener {
        void onChatUpdated();
    }

    private OnChatUpdateListener chatUpdateListener;

    public void setOnChatUpdateListener(OnChatUpdateListener listener) {
        this.chatUpdateListener = listener;
    }

    private void notifyChatUpdated() {
        if (chatUpdateListener != null) {
            chatUpdateListener.onChatUpdated();
        }
    }


    public void addChannel(String channel) {
        if (!channels.contains(channel)) {
            channels.add(channel);
            notifyChatUpdated();
        }
    }

    public synchronized void addPrivateMessage(String sender, String recipient, String message) {
        privateMessages.add(sender + ": " + message);
        System.out.println("Added private message: " + message);

        SharedDataSource.getInstance().storeMessage(sender, message);
        // Notify all listeners
        for (Runnable listener : privateMessageListeners) {
            listener.run();
        }
    }

    public synchronized List<String> getMessagesFor(String channel) {
        return messages.stream().filter(msg -> msg.startsWith(channel + ":")).collect(Collectors.toList());

    }

    public synchronized List<String> getPrivateMessagesFor(String senderOrRecipient) {
        // Fetch messages from GlobalMessageListener's privateMessages list
        List<String> localMessages = privateMessages.stream()
                .filter(msg -> msg.startsWith(senderOrRecipient + ":"))
                .collect(Collectors.toList());

        // Fetch messages from SharedDataSource
        List<String> sharedDataMessages = SharedDataSource.getInstance().getStoredMessages(senderOrRecipient);

        // Combine both lists
        localMessages.addAll(sharedDataMessages);

        return localMessages;
    }






    public synchronized void addUserToNamesList(String name) {
        namesList.add(name);
    }

    public synchronized void addPrivateChat(String username) {
        if (!privateChats.contains(username)) {
            privateChats.add(username);
            notifyChatUpdated();
            SharedDataSource.getInstance().getCombinedList().add(username);

        }
    }

    public void onNewMessage(String channelOrUser, String sender, String message) {
        String fullMessage = sender + ": " + message;
        SharedDataSource.getInstance().storeMessage(channelOrUser, fullMessage);
    }


    public synchronized boolean isUserInPrivateChats(String username) {
        return privateChats.contains(username);
    }

    public synchronized List<String> getPrivateChats() {
        return new ArrayList<>(privateChats);
    }

    public synchronized List<String> getChannels() {
        return new ArrayList<>(channels);
    }

    public synchronized List<String> getNamesList() {
        return new ArrayList<>(namesList);
    }

    public synchronized void addPrivateMessageListener(Runnable listener) {
        privateMessageListeners.add(listener);
    }

    public synchronized void removePrivateMessageListener(Runnable listener) {
        privateMessageListeners.remove(listener);
    }
}
