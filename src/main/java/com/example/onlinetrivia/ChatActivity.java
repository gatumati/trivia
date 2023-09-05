package com.example.onlinetrivia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.onlinetrivia.models.ChatMessage;
import com.example.onlinetrivia.models.DatabaseInitializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Button btnOpenLeftDrawer;
    private ListView chatsListView;
    private TextView chatTextView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private ChatHelper chatHelper;
    private String currentChat;
    private Map<String, StringBuilder> chatHistories = new HashMap<>();
    private List<String> channels = new ArrayList<>();
    private List<String> privateChats = new ArrayList<>();
    private IRCClient ircClient;  // Assuming ircClient is a member of ChatActivity for fetching user's nickname.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        drawerLayout = findViewById(R.id.drawer_layout);
        btnOpenLeftDrawer = findViewById(R.id.btnOpenLeftDrawer);
        chatsListView = findViewById(R.id.chatsListView);
        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        if (chatsListView.getLayoutParams() instanceof DrawerLayout.LayoutParams) {
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) chatsListView.getLayoutParams();
            // Other code...
        } else {
            Log.e("ERROR_TAG", "View's layout parameters are not of type DrawerLayout.LayoutParams");
        }

        chatHelper = new ChatHelper(this);
        ircClient = IRCClient.getInstance();

        if (getIntent().hasExtra("chatTarget")) {
            currentChat = stripPrefixes(getIntent().getStringExtra("chatTarget"));
            loadChatHistory(currentChat);
        }
        btnOpenLeftDrawer.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(View drawerView) {
                btnOpenLeftDrawer.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                btnOpenLeftDrawer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        chatHelper.setPrivateMessageListener((sender, message) -> {
            runOnUiThread(() -> {
                updateChatHistory(stripPrefixes(sender), sender + ": " + message);
                if (!privateChats.contains(stripPrefixes(sender))) {
                    privateChats.add(stripPrefixes(sender));
                    refreshDrawerList();
                }
            });
        });

        chatHelper.setChannelMessageListener((channel, sender, message) -> {
            updateChatHistory("#" + stripPrefixes(channel), sender + ": " + message);
        });

        chatHelper.setChatListUpdatedListener(chatList -> {
            runOnUiThread(() -> {
                channels.clear();
                privateChats.clear();
                for (String item : chatList) {
                    if (isChannel(item)) {
                        channels.add(item);
                    } else {
                        privateChats.add(stripPrefixes(item));
                    }
                }
                refreshDrawerList();
            });
        });
        chatsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedChat = stripPrefixes((String) parent.getItemAtPosition(position));

            if (isChannel(selectedChat)) {
                // If selected item is a channel, switch to the ChannelActivity.
                Intent intent = new Intent(ChatActivity.this, ChannelActivity.class);
                intent.putExtra("channel_name", selectedChat);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else {
                // If selected item is a private message, load the chat history.
                loadChatHistory(selectedChat);

            }
            currentChat = stripPrefixes((String) parent.getItemAtPosition(position));
            loadChatHistory(currentChat);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        sendMessageButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty() && currentChat != null) {
                String cleanedChatName = stripPrefixes(currentChat);
                String userNickname = ircClient.getNickname();  // Fetch the user's nickname using the IRC client.
                String formattedMessage = userNickname.equals(ircClient.getNickname()) ? userNickname + ": " + message : "You: " + message;

                if (isChannel(cleanedChatName)) {
                    chatHelper.sendChannelMessage(cleanedChatName, message);
                } else {
                    chatHelper.sendMessage(cleanedChatName, message);
                }

                // Add the recipient to the private chat list if they aren't already present
                if (!privateChats.contains(cleanedChatName)) {
                    privateChats.add(cleanedChatName);
                    refreshDrawerList();
                }

                updateChatHistory(cleanedChatName, formattedMessage);
                messageEditText.setText("");
            }
        });
    }

    private String stripPrefixes(String name) {
        return name.replaceAll("^[@+]", "");
    }

    private boolean isChannel(String name) {
        return name.startsWith("#");
    }

    private void refreshDrawerList() {
        // Sort channels alphabetically
        Collections.sort(channels);

        // Combine channels and private chats
        List<String> combinedList = new ArrayList<>();
        combinedList.addAll(channels);
        combinedList.addAll(privateChats);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, combinedList);
        chatsListView.setAdapter(adapter);
    }

    private void updateChatHistory(String chatName, String newMessageContent) {
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
            DatabaseInitializer.getInstance(ChatActivity.this)
                    .chatMessageDao().insert(newMessage);
        }).start();

        if (chatName.equals(currentChat)) {
            runOnUiThread(() -> chatTextView.setText(history.toString()));
        }
    }

    private void loadChatHistory(String chatName) {
        new Thread(() -> {
            List<ChatMessage> messages = DatabaseInitializer.getInstance(ChatActivity.this)
                    .chatMessageDao().getMessagesForChat(chatName);

            Collections.reverse(messages);

            StringBuilder history = new StringBuilder();
            for (ChatMessage message : messages) {
                String formattedMessage = message.getSender() + ": " + message.getMessageContent();
                history.append(formattedMessage).append("\n");
            }

            chatHistories.put(chatName, history);
            String finalHistoryContent = history.toString();

            runOnUiThread(() -> {
                chatTextView.setText(finalHistoryContent);
            });
        }).start();
    }
}
