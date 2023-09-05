package com.example.onlinetrivia;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.List;

public class ChatHelper {

    private IRCClient ircClient;

    // Listener for private messages
    private OnPrivateMessageReceivedListener privateMessageListener;

    // Listener for channel messages
    private OnChannelMessageReceivedListener channelMessageListener;

    // Android context to start new activities and perform other Android-specific operations
    private Context context;



    // Listener for chat list updates
    public interface OnChatListUpdatedListener {
        void onChatListUpdated(List<String> chatList);
    }

    private OnChatListUpdatedListener chatListListener;

    public interface OnPrivateMessageReceivedListener {
        void onPrivateMessageReceived(String sender, String message);
    }

    public ChatHelper(Context context) {
        this.context = context;
        this.ircClient = IRCClient.getInstance();
        initializeListeners();
    }

    private void initializeListeners() {
        // Listener for channel messages
        ircClient.setMessageListener((channel, sender, message) -> {
            // Handle channel messages here
            // Since it's no longer handling private messages, this may be empty unless you want additional behavior
        });

        // Listener for private messages
        ircClient.setPrivateMessageListener((sender, message) -> {
            if (privateMessageListener != null) {
                privateMessageListener.onPrivateMessageReceived(sender, message);
            }
        });

        // Listener for chat list updates
        ircClient.setChatListUpdatedListener(chatList -> {
            Log.d("ChatHelper", "Chat list update detected.");
            if (chatListListener != null) {
                chatListListener.onChatListUpdated(chatList);
                Log.d("ChatHelper", "chatListListener is null.");
            }
        });
    }

    public void setPrivateMessageListener(OnPrivateMessageReceivedListener listener) {
        this.privateMessageListener = listener;
    }

    public void setChatListUpdatedListener(OnChatListUpdatedListener listener) {
        this.chatListListener = listener;

    }

    public void sendMessage(String recipient, String message) {
        Log.d("ChatHelper", "Sending message to: " + recipient + ", Message: " + message);
        ircClient.sendMessageToChannel(recipient, message); // Using the same method for channel and private messages
    }

    public interface OnChannelMessageReceivedListener {
        void onChannelMessageReceived(String channel, String sender, String message);
    }

    private void initializeChannelMessageListener() {
        ircClient.setChannelMessageListener((channel, sender, message) -> {
            if (channelMessageListener != null) {
                channelMessageListener.onChannelMessageReceived(channel, sender, message);
            }
        });
    }

    public void setChannelMessageListener(OnChannelMessageReceivedListener listener) {
        this.channelMessageListener = listener;
    }

    public void sendChannelMessage(String channel, String message) {
        Log.d("ChatHelper", "Sending channel message to: " + channel + ", Message: " + message);
        ircClient.sendMessageToChannel(channel, message);
    }

    public void startPrivateChat(String targetUser) {
        Log.d("ChatHelper", "Starting private chat with: " + targetUser);
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra("chatTarget", targetUser);
        context.startActivity(chatIntent);
    }
}