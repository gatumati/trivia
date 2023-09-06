package com.example.onlinetrivia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatHelper {
    private Context context;
    private IRCClient ircClient;
    private StringBuilder chatHistory;
    private ArrayAdapter<String> drawerAdapter;
    private ArrayAdapter<String> connectedChannelsAdapter;
    private ArrayAdapter<String> channelListAdapter;

    public ChatHelper(Context context) {
        this.context = context;
        this.ircClient = IRCClient.getInstance();
        this.chatHistory = new StringBuilder();
    }

    public void handleChatSelection(String selectedChat) {
        if (isChannel(selectedChat)) {
            switchToChannelActivity(selectedChat);
        } else {
            loadChatHistory(selectedChat);
        }
    }

    private boolean isChannel(String chatName) {
        return chatName.startsWith("#");
    }

    private void switchToChannelActivity(String channelName) {
        Intent intent = new Intent(context, ChannelActivity.class);
        intent.putExtra("channel_name", channelName);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    public void loadChatHistory(String chatName) {
        chatHistory = new StringBuilder();
        List<String> messages = ircClient.getChatHistory(chatName);
        for (String message : messages) {
            chatHistory.append(message).append("\n");
        }
    }

    public String stripPrefixes(String name) {
        if (name.startsWith("@")) {
            return name.substring(1);
        }
        return name;
    }

    public void updateNamesList(List<String> names) {
        Collections.sort(names, (o1, o2) -> {
            if (o1.startsWith("@") && !o2.startsWith("@")) {
                return -1;
            } else if (!o1.startsWith("@") && o2.startsWith("@")) {
                return 1;
            }
            return o1.compareToIgnoreCase(o2);
        });
    }

    public void startPrivateChat(String targetUser) {
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra("chatTarget", targetUser);
        context.startActivity(chatIntent);
    }

    public void handleSendMessage(String message, ChatType currentChatType, String currentChatName) {
        if (!message.isEmpty()) {
            if (currentChatType == ChatType.PRIVATE) {
                // Logic for private message
            } else {
                ircClient.sendMessage(currentChatName, message);
            }
        }
    }

    public void setupListeners() {
        // Setting up listeners for messages, names, and private messages
    }

    public void refreshDrawerList() {
        List<String> combinedList = new ArrayList<>();
        combinedList.addAll(ircClient.getChannels());
        combinedList.addAll(ircClient.getPrivateChats());
        drawerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, combinedList);
    }

    public void updateConnectedChannelsList() {
        connectedChannelsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, ircClient.getChannels());
    }

    public void switchToChannel(String channelName, ChatType currentChatType) {
        currentChatName = channelName;
        currentChatType = ChatType.CHANNEL;
        loadChatHistory(channelName);
    }

    public void updateChannelList() {
        channelListAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, ircClient.getChannels());
    }

    public void onNewMessageReceived(String message) {
        chatHistory.append(message);
    }
    public void displayChatHistory() {
        chatHistory = new StringBuilder();
        List<String> messages = ircClient.getChatHistory(currentChatName);
        for (String message : messages) {
            chatHistory.append(message).append("\n");
        }
    }

    public void onBackPressed(long backPressedTime) {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            ((Activity) context).onBackPressed();
        }
    }

    public void createAndShowToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void onDestroy() {
        if (ircClient != null) {
            ircClient.removeMessageListener(messageListener);
            ircClient.removeNamesListener(namesListener);
        }
    }

    public void appendIrcMessage(String message) {
        chatHistory.append(message);
    }

    public void setupDrawerListeners() {
        // Logic for setting up listeners for drawer open, close, and message receive
    }


}
