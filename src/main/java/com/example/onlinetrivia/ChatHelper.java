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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // New member variables for private message handling
    private Map<String, StringBuilder> chatHistories = new HashMap<>();

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

    private String stripPrefixes(String name) {
        return name.replaceAll("^[@+]", "");
    }


    public void handleNamesItemClick(String selectedName) {
        startPrivateChat(selectedName);
        if(drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    public void handleChatsItemClick(String selectedItem) {
        if (privateChats.contains(selectedItem)) {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("chatTarget", selectedItem);
            context.startActivity(chatIntent);
        } else {
            switchToChannel(selectedItem);
        }
        drawerLayout.closeDrawers();
    }

    public void initializeListeners() {
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

        ircClient.setPrivateMessageListener((sender, message) -> {
            GlobalMessageListener.getInstance().addPrivateMessage(sender, ircClient.getNickname(), message);
            if (privateMessageListener != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    privateMessageListener.onPrivateMessageReceived(sender, message);
                });
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
        Log.d("ChatHelper", "Original recipient: " + recipient);
        recipient = stripPrefixes(recipient);
        Log.d("ChatHelper", "Stripped recipient: " + recipient);

        Log.d("ChatHelper", "Original message: " + message);
        message = stripPrefixes(message);
        Log.d("ChatHelper", "Stripped message: " + message);

        String currentNickname = ircClient.getNickname();
        appendIrcMessage(currentNickname + ": " + message);


        ircClient.sendMessageToChannel(recipient, message);
        GlobalMessageListener.getInstance().addPrivateMessage(ircClient.getNickname(), recipient, message);
    }


    public void setChannelMessageListener(OnChannelMessageReceivedListener listener) {
        this.channelMessageListener = listener;
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
        chatHistory.add(coloredMessage.toString());
        Log.d("ChatHelper", "Appended message to chatHistory: " + fullMessage);
        new Handler(Looper.getMainLooper()).post(() -> {
            chatTextView.append(coloredMessage);
            chatTextView.append("\n");
        });
    }

    public String updateChatDisplay(String message) {
        Spannable spannable = MircColors.toSpannable(message);
        return spannable.toString();
    }

    public void displayChatHistory(TextView chatTextView) {
        chatTextView.setText(""); // Clear the TextView
        for (String rawMessage : chatHistory) {
            Spannable coloredMessage = MircColors.toSpannable(rawMessage);
            chatTextView.append(coloredMessage);
            chatTextView.append("\n");
        }
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

    public void onDestroy() {
        if (ircClient != null) {
            ircClient.setMessageListener(null);
            ircClient.setNamesListener(null);
        }
    }
}

