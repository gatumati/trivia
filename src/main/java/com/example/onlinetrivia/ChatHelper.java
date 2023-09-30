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

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHelper {



    private static ChatHelper instance = new ChatHelper();

    // Private constructor to prevent creating multiple instances
    private ChatHelper() {
    }

    // Public method to get the single instance of the class
    public static ChatHelper getInstance() {
        if (instance == null) {
            instance = new ChatHelper();
        }
        return instance;
    }


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

    private Map<String, List<String>> channelContentMap = new HashMap<>();

    private List<String> connectedChannels = new ArrayList<>();
    private List<String> activePrivateChats = new ArrayList<>();

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

    // Methods to manipulate the connected channels
    public void addConnectedChannel(String channel) {
        if (!connectedChannels.contains(channel)) {
            connectedChannels.add(channel);
        }
    }

    public void removeConnectedChannel(String channel) {
        connectedChannels.remove(channel);
    }

    public List<String> getConnectedChannels() {
        return new ArrayList<>(connectedChannels);
    }

    // Methods to manipulate the active private chats
    public void addActivePrivateChat(String user) {
        if (!activePrivateChats.contains(user)) {
            activePrivateChats.add(user);
        }
    }

    public void removeActivePrivateChat(String user) {
        activePrivateChats.remove(user);
    }

    public List<String> getActivePrivateChats() {
        return new ArrayList<>(activePrivateChats);
    }

    public void joinChannel(String channel) {

        addConnectedChannel(channel);
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
        if (name == null) {
            return "";
        }
        return name.replaceAll("^[@+]", "");
    }

    public interface OnDrawerListRefreshRequestedListener {
        void onRefreshRequested();
    }

    private OnDrawerListRefreshRequestedListener drawerListRefreshListener;

    public void setOnDrawerListRefreshRequestedListener(OnDrawerListRefreshRequestedListener listener) {
        this.drawerListRefreshListener = listener;
    }



    public void handleNamesItemClick(String selectedName) {
        startPrivateChat(selectedName);
        if(drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
    }

    public void handleChatsItemClick(String selectedItem) {
        if (privateChats.contains(selectedItem)) {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatTarget", selectedItem); // Assuming 'sender' is the user who sent the private message
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);
        } else {
            switchToChannel(selectedItem);
        }
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
                if (!GlobalMessageListener.getInstance().isUserInPrivateChats(sender)) {
                    GlobalMessageListener.getInstance().addPrivateChat(sender);
                    SharedDataSource.getInstance().handlePrivateMessage(sender, message);
                    Log.d("ChatHelper", "Sending Private Message To: " + sender);
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("chatTarget", sender); // Assuming 'sender' is the user who sent the private message
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                    addActivePrivateChat(sender);



                    if (drawerListRefreshListener != null) {
                        drawerListRefreshListener.onRefreshRequested();
                    }


                }
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
            SharedDataSource.getInstance().handlePrivateMessage(sender, message);
            Log.d("ChatHelper", "Reciving Private Message From: " + sender);
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatTarget", sender); // Assuming 'sender' is the user who sent the private message
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);
            addActivePrivateChat(sender);

            if (drawerListRefreshListener != null) {
                drawerListRefreshListener.onRefreshRequested();
            }

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
        if (recipient != null) {
            recipient = stripPrefixes(recipient);
        }
        Log.d("ChatHelper", "Stripped recipient: " + recipient);

        Log.d("ChatHelper", "Original message: " + message);
        if (message != null) {
            message = stripPrefixes(message);
        }
        Log.d("ChatHelper", "Stripped message: " + message);

        String currentNickname = ircClient.getNickname();
        appendIrcMessage(currentNickname + ": " + message);


        ircClient.sendMessageToChannel(recipient, message);
        GlobalMessageListener.getInstance().addPrivateMessage(ircClient.getNickname(), recipient, message);
        Log.d("ChatHelper", "Sending message to recipient: " + recipient + " with message: " + message);
        SharedDataSource.getInstance().storeMessage(recipient, message);
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

    public boolean isActiveChannel(String channel) {
        return connectedChannels.contains(channel);
    }

    public boolean isActivePrivateChat(String user) {
        return activePrivateChats.contains(user);
    }


    public void startPrivateChat(String targetUser) {

        Log.d("ChatHelper", "Starting private chat with: " + targetUser);
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("chatTarget", targetUser); // Assuming 'sender' is the user who sent the private message
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
        addActivePrivateChat(targetUser);
    }

    private void appendIrcMessage(String fullMessage) {
        Spannable coloredMessage = MircColors.toSpannable(fullMessage);
        chatHistory.add(coloredMessage.toString());
        Log.d("ChatHelper", "Appended message to chatHistory: " + fullMessage);

        // Store the message in SharedDataSource
        SharedDataSource.getInstance().storeMessage(channelName, coloredMessage.toString());



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
        for (String rawMessage : chatHistory) {
            Spannable coloredMessage = MircColors.toSpannable(rawMessage);
            chatTextView.append(coloredMessage);
            chatTextView.append("\n");
        }

        // Retrieve and display stored messages from SharedDataSource
        List<String> storedMessages = SharedDataSource.getInstance().getStoredMessages(channelName);

        for (String message : storedMessages) {
            Spannable coloredMessage = MircColors.toSpannable(message);
            chatTextView.append(coloredMessage);
            chatTextView.append("\n");
        }
    }


    public void switchToChannel(String newChannel) {
        if (context instanceof ChannelActivity) {
            // If already in ChannelActivity, update the current activity
            ChannelActivity currentActivity = (ChannelActivity) context;
            List<String> channelMessages = channelContentMap.getOrDefault(newChannel, new ArrayList<>());
            currentActivity.updateChannelContent(newChannel, channelMessages);
        } else {
            // If not in ChannelActivity, start the ChannelActivity
            Intent channelIntent = new Intent(context, ChannelActivity.class);
            channelIntent.putExtra("CHANNEL_NAME", newChannel);
            context.startActivity(channelIntent);
        }
    }

    public void addMessageToChannel(String channel, String message) {
        List<String> channelMessages = channelContentMap.getOrDefault(channel, new ArrayList<>());
        channelMessages.add(message);
        channelContentMap.put(channel, channelMessages);
    }


    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }


    public void handleSendMessage(String channel, String message) {
        if (!message.isEmpty() && ircClient != null && channel != null) {
            ircClient.sendMessageToChannel(channel, message);
            String currentNickname = ircClient.getNickname();
            appendIrcMessage(currentNickname + ": " + message);
            Log.d("ChatHelper", "Handling send message to channel: " + channel + " with message: " + message);
        }
    }

    public void onDestroy() {
        if (ircClient != null) {
            ircClient.setMessageListener(null);
            ircClient.setNamesListener(null);
        }
    }
}
