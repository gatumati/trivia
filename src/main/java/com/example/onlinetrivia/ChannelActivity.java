package com.example.onlinetrivia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelActivity extends Activity implements AdapterView.OnItemClickListener {
    private boolean doubleBackToExitPressedOnce = false;

        enum ChatType {
            CHANNEL, PRIVATE
        }

        private ChatType currentChatType = ChatType.CHANNEL; // default to channel

    private TextView chatTextView;
    private EditText messageEditText;
    private Button sendMessageButton, btnOpenRightDrawer, btnOpenLeftDrawer;
    private ListView namesListView, chatsListView;
    private DrawerLayout drawerLayout;
    private IRCClient ircClient;
    private String channelName;
    private String selectedUser;
    private Toast exitToast;
    private List<String> connectedChannels;
    private ChatHelper chatHelper;
    private List<String> channels = new ArrayList<>();
    private List<String> privateChats = new ArrayList<>();
    private List<String> chatHistory = new ArrayList<>();


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = (String) parent.getItemAtPosition(position);
        Intent chatIntent;

        if (privateChats.contains(selectedItem)) {
            chatIntent = new Intent(ChannelActivity.this, ChatActivity.class);
            chatIntent.putExtra("chatTarget", selectedItem);
        } else {
            chatIntent = new Intent(ChannelActivity.this, ChannelActivity.class);
            chatIntent.putExtra("channel_name", selectedItem);
        }
        startActivity(chatIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        if (getIntent() != null) {
            selectedUser = getIntent().getStringExtra("selected_user");
        }

        chatsListView = findViewById(R.id.chatsListView);

        initializeComponents();
        setupListeners();
        setupDrawerListeners();
        updateChannelList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupListeners(); // Re-setup the listeners.
        List<String> oldMessages = GlobalMessageListener.getInstance().getMessagesFor(channelName);
        for (String message : oldMessages) {
            appendIrcMessage(message);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (ircClient != null) {
            ircClient.setMessageListener(null);
            ircClient.setNamesListener(null);
        }
    }


    private void initializeComponents() {
        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        btnOpenRightDrawer = findViewById(R.id.btnOpenDrawer);
        btnOpenLeftDrawer = findViewById(R.id.btnOpenLeftDrawer);
        namesListView = findViewById(R.id.namesListView);
        chatsListView = findViewById(R.id.chatsListView);
        drawerLayout = findViewById(R.id.drawer_layout);
        ircClient = IRCClient.getInstance();
        connectedChannels = new ArrayList<>();
        sendMessageButton.setOnClickListener(view -> handleSendMessage());


        chatHelper = new ChatHelper(this);

        if (getIntent().hasExtra("channel_name")) {
            channelName = getIntent().getStringExtra("channel_name");
            setTitle(channelName);
            connectedChannels.add(channelName);
            updateConnectedChannelsList();
        }
    }

    private void handleSendMessage() {
        String message = messageEditText.getText().toString();
        if (!message.isEmpty() && ircClient != null && channelName != null) {
            ircClient.sendMessageToChannel(channelName, message);
            if (currentChatType == ChatType.PRIVATE) {
            }
            String currentNickname = ircClient.getNickname();
            appendIrcMessage(currentNickname + ": " + message);

            messageEditText.setText("");
        }
    }

    private void setupListeners() {
        ircClient.setMessageListener((channel, sender, message) -> {
            if (channel.equals(channelName)) {
                appendIrcMessage(sender + ": " + message);
                GlobalMessageListener.getInstance().addMessage(channel, sender + ": " + message);
            }
        });
        ircClient.setNamesListener(this::updateNamesList);

        // Listener for private messages
        chatHelper.setPrivateMessageListener((sender, message) -> {
            runOnUiThread(() -> {
                if (!privateChats.contains(sender)) {
                    privateChats.add(sender);
                    refreshDrawerList();
                }
            });
        });
    }

    private void refreshDrawerList() {
        Collections.sort(channels);

        // Combine channels and private chats
        List<String> combinedList = new ArrayList<>();
        combinedList.addAll(channels);
        combinedList.addAll(privateChats);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, combinedList);
        chatsListView.setAdapter(adapter);
    }

    private void setupDrawerListeners() {
        btnOpenRightDrawer.setOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.END);
            ircClient.joinChannel(channelName);
        });

        ircClient.setMessageListener((channel, sender, message) -> {
            if (channel.equals(channelName) || sender.equals(selectedUser)) {  // Ensure the message is for the current channel or private chat
                String formattedMessage = sender + ": " + message;
                onNewMessageReceived(formattedMessage);  // Call the method when a new message is received
            }
        });

        btnOpenLeftDrawer.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerView.findViewById(R.id.namesListView) != null) {
                    btnOpenRightDrawer.setVisibility(View.GONE);
                } else if (drawerView.findViewById(R.id.chatsListView) != null) {
                    btnOpenLeftDrawer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                btnOpenRightDrawer.setVisibility(View.VISIBLE);
                btnOpenLeftDrawer.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateConnectedChannelsList() {
        runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ChannelActivity.this, android.R.layout.simple_list_item_1, connectedChannels);
            chatsListView.setAdapter(adapter);

            chatsListView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedItem = (String) parent.getItemAtPosition(position);
                if (privateChats.contains(selectedItem)) {
                    Intent chatIntent = new Intent(ChannelActivity.this, ChatActivity.class);
                    chatIntent.putExtra("chatTarget", selectedItem);
                    chatIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(chatIntent);
                } else {
                    switchToChannel(selectedItem);
                }
                drawerLayout.closeDrawers();
            });

        });
    }

    private void switchToChannel(String newChannel) {
        channelName = newChannel;
        currentChatType = ChatType.CHANNEL;
        setTitle(channelName);
        List<String> oldMessages = GlobalMessageListener.getInstance().getMessagesFor(newChannel);
    }

    public void updateChannelList() {
        channels = ircClient.getCurrentChatList();
        refreshDrawerList();
    }

    private void onNewMessageReceived(String message) {
        chatHistory.add(message);
        displayChatHistory();
    }

    private void displayChatHistory() {
        chatTextView.setText(""); // Clear the TextView
        for (String message : chatHistory) {
            chatTextView.append(message + "\n");
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();  // Close all activities of this app task
            System.exit(0);    // Force exit
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        createAndShowToast("Press again to exit.");

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void createAndShowToast(String message) {
        if (exitToast != null) {
            exitToast.cancel();
        }

        exitToast = Toast.makeText(ChannelActivity.this, message, Toast.LENGTH_SHORT);
        exitToast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ircClient != null) {
            ircClient.setMessageListener(null);
            ircClient.setNamesListener(null);
        }
    }

    private void appendIrcMessage(String fullMessage) {
        runOnUiThread(() -> {
            chatTextView.append("\n" + fullMessage);
            chatTextView.postDelayed(() -> {
                ScrollView scrollView = (ScrollView) chatTextView.getParent();
                scrollView.fullScroll(View.FOCUS_DOWN);
            }, 100);
        });
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

        runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ChannelActivity.this, android.R.layout.simple_list_item_1, names);
            namesListView.setAdapter(adapter);
            namesListView.invalidate();

            namesListView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedName = (String) parent.getItemAtPosition(position);
                currentChatType = ChatType.PRIVATE;
                chatHelper.startPrivateChat(selectedName);
                drawerLayout.closeDrawers();
            });
        });
    }
}

