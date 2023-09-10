package com.example.onlinetrivia;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.text.Spannable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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

    private DrawerLayout drawerLayout;
    private Button btnOpenRightDrawer, btnOpenLeftDrawer;

    // Listener interfaces
    private OnPrivateMessageReceivedListener privateMessageListener;
    private OnChannelMessageReceivedListener channelMessageListener;
    private OnChatListUpdatedListener chatListListener;

    public interface OnPrivateMessageReceivedListener {
        void onPrivateMessageReceived(String sender, String message);
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

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    public void setDrawerButtons(Button btnOpenRightDrawer, Button btnOpenLeftDrawer) {
        this.btnOpenRightDrawer = btnOpenRightDrawer;
        this.btnOpenLeftDrawer = btnOpenLeftDrawer;

        // Set onClick listeners for the buttons
        btnOpenRightDrawer.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.END));
        btnOpenLeftDrawer.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
    }

    public void handleNamesItemClick(String selectedName) {
        // Logic for handling item click in the names list (right drawer)
        startPrivateChat(selectedName);
        drawerLayout.closeDrawers();
    }

    public void handleChatsItemClick(String selectedItem) {
        // Logic for handling item click in the chats list (left drawer)
        if (privateChats.contains(selectedItem)) {
            // Start private chat activity
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("chatTarget", selectedItem);
            context.startActivity(chatIntent);
        } else {
            // Switch to the selected channel
            switchToChannel(selectedItem);
        }
        drawerLayout.closeDrawers();
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

    public String updateChatDisplay(String message) {
        // Convert MircColors to Android Spannable
        Spannable spannable = MircColors.toSpannable(message);
        return spannable.toString();
    }

    public void switchToChannel(String newChannel) {
        channelName = newChannel;
        List<String> oldMessages = GlobalMessageListener.getInstance().getMessagesFor(newChannel);
        chatHistory.addAll(oldMessages);
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void refreshDrawerList(ListView chatsListView) {
        Collections.sort(channels);

        // Combine channels and private chats
        List<String> combinedList = new ArrayList<>();
        combinedList.addAll(channels);
        combinedList.addAll(privateChats);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, combinedList);
        chatsListView.setAdapter(adapter);
    }

    public void handleSendMessage(String channel, String message) {
        if (!message.isEmpty() && ircClient != null && channel != null) {
            ircClient.sendMessageToChannel(channel, message);
            String currentNickname = ircClient.getNickname();
            appendIrcMessage(currentNickname + ": " + message);
        }
    }

    public void setupDrawerListeners(ListView chatsListView, ListView namesListView, String selectedUser) {
        chatsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            if (privateChats.contains(selectedItem)) {
                Intent chatIntent = new Intent(context, ChatActivity.class);
                chatIntent.putExtra("chatTarget", selectedItem);
                context.startActivity(chatIntent);
            } else {
                switchToChannel(selectedItem);
            }
        });

        namesListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            startPrivateChat(selectedName);
        });
    }

    public void displayChatHistory(TextView chatTextView) {
        chatTextView.setText(""); // Clear the TextView
        for (String rawMessage : chatHistory) {
            Spannable coloredMessage = MircColors.toSpannable(rawMessage);
            chatTextView.append(coloredMessage);
            chatTextView.append("\n");
        }
    }

    public void onDestroy() {
        if (ircClient != null) {
            ircClient.setMessageListener(null);
            ircClient.setNamesListener(null);
        }
    }

    public String stripPrefixes(String name) {
        return name.replaceAll("^[@+]", "");
    }
}
