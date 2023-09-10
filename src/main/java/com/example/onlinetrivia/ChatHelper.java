package com.example.onlinetrivia;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.onlinetrivia.models.ChatMessage;
import com.example.onlinetrivia.models.DatabaseInitializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatHelper {

    private IRCClient ircClient;
    private Context context;
    private List<String> channels = new ArrayList<>();
    private List<String> privateChats = new ArrayList<>();
    private List<String> chatHistory = new ArrayList<>();
    private String channelName;
    private TextView chatTextView;
    private ListView namesListView, chatsListView;
    private OnChatHistoryUpdatedListener chatHistoryListener;

    private DrawerLayout drawerLayout;
    private Button btnOpenRightDrawer, btnOpenLeftDrawer;

    // Listener interfaces
    private OnPrivateMessageReceivedListener privateMessageListener;
    private OnChannelMessageReceivedListener channelMessageListener;
    private OnChatListUpdatedListener chatListListener;

    public interface OnPrivateMessageReceivedListener {
        void onPrivateMessageReceived(String sender, String message);
    }

    public interface OnChatHistoryUpdatedListener {
        void onChatHistoryUpdated(String updatedHistory);
    }

    public void setChatHistoryUpdatedListener(OnChatHistoryUpdatedListener listener) {
        this.chatHistoryListener = listener;
    }

    public interface OnChannelMessageReceivedListener {
        void onChannelMessageReceived(String channel, String sender, String message);
    }

    public interface OnChatListUpdatedListener {
        void onChatListUpdated(List<String> chatList);
    }

    public ChatHelper(Context context) {
        this.context = context;
        this.ircClient = IRCClient.getInstance();
        initializeListeners();
    }

    private void initializeListeners() {
        // Listener for channel messages
        ircClient.setMessageListener((channel, sender, message) -> {
            Log.d("ChatHelper", "Received message for channel: " + channel);
            if (channel.equals(channelName)) {
                appendIrcMessage(sender + ": " + message);
                GlobalMessageListener.getInstance().addMessage(channel, sender + ": " + message);
            }
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
        ircClient.sendMessageToChannel(recipient, message);
    }

    public void setChannelMessageListener(OnChannelMessageReceivedListener listener) {
        this.channelMessageListener = listener;
    }

    public void sendChannelMessage(String channel, String message) {
        Log.d("ChatHelper", "Sending channel message to: " + channel + ", Message: " + message);
        ircClient.sendMessageToChannel(channel, message);
    }

    public void setChatTextView(TextView chatTextView) {
        this.chatTextView = chatTextView;
    }

    public void setNamesListView(ListView namesListView) {
        this.namesListView = namesListView;
    }

    public void setChatsListView(ListView chatsListView) {
        this.chatsListView = chatsListView;
    }

    public void startPrivateChat(String targetUser) {
        Log.d("ChatHelper", "Starting private chat with: " + targetUser);
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra("chatTarget", targetUser);
        context.startActivity(chatIntent);
    }

    private void appendIrcMessage(String fullMessage) {
        Spannable coloredMessage = MircColors.toSpannable(fullMessage);
        chatHistory.add(coloredMessage.toString()); // Store the plain text for history
        Log.d("ChatHelper", "Appended message to chatHistory: " + fullMessage);
        new Handler(Looper.getMainLooper()).post(() -> {
            chatTextView.append(coloredMessage);
            chatTextView.append("\n");
        });
    }

    public void displayChatHistory() {
        new Handler(Looper.getMainLooper()).post(() -> {
            for (String message : chatHistory) {
                chatTextView.append(message + "\n");
            }
        });
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    public void setBtnOpenRightDrawer(Button btnOpenRightDrawer) {
        this.btnOpenRightDrawer = btnOpenRightDrawer;
    }

    public void setBtnOpenLeftDrawer(Button btnOpenLeftDrawer) {
        this.btnOpenLeftDrawer = btnOpenLeftDrawer;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void updateChatHistory(String chatName, String newMessageContent) {
        StringBuilder history = chatHistories.getOrDefault(chatName, new StringBuilder());
        history.append("\n").append(newMessageContent);
        chatHistories.put(chatName, history);

        String sender;
        if (newMessageContent.startsWith(ircClient.getNickname() + ": ")) {
            sender = ircClient.getNickname();
        } else {
            sender = newMessageContent.split(":")[0];
        }

        new Thread(() -> {
            ChatMessage newMessage = new ChatMessage(chatName, sender, newMessageContent.replace(sender + ": ", ""), System.currentTimeMillis());
            DatabaseInitializer.getInstance(context)
                    .chatMessageDao().insert(newMessage);
        }).start();

        if (chatHistoryListener != null) {
            chatHistoryListener.onChatHistoryUpdated(history.toString());
        }
    }

    public void loadChatHistory(String chatName) {
        new Thread(() -> {
            List<ChatMessage> messages = DatabaseInitializer.getInstance(context)
                    .chatMessageDao().getMessagesForChat(chatName);

            Collections.reverse(messages);

            StringBuilder history = new StringBuilder();
            for (ChatMessage message : messages) {
                String formattedMessage = message.getSender() + ": " + message.getMessageContent();
                history.append(formattedMessage).append("\n");
            }

            chatHistories.put(chatName, history);
            String finalHistoryContent = history.toString();

            if (chatHistoryListener != null) {
                chatHistoryListener.onChatHistoryUpdated(finalHistoryContent);
            }
        }).start();

        public String stripPrefixes (String name){
            return name.replaceAll("^[@+]", "");
        }
    }
}