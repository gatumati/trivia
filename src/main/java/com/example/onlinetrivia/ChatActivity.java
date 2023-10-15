package com.example.onlinetrivia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private TextView chatTextView;
    private EditText messageEditText;
    private Button sendMessageButton, btnOpenLeftDrawer;
    private ChatHelper chatHelper;
    private DrawerLeft drawerLeft;
    private ListView chatsListView;
    private DrawerLayout drawerLayout;

    private String currentChatUser;

    private IRCClient ircClient;

    private ArrayAdapter<String> adapter;

    private String targetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        targetUser = getIntent().getStringExtra("chatTarget");

        if (targetUser != null) {
            // Use the targetUser variable
        } else {
            Log.e("ChatActivity", "targetUser is null");
        }

        final String targetUser = getIntent().getStringExtra("chatTarget");
        Log.d("ChatActivity", "onCreate called for user: " + targetUser);




        chatHelper = new ChatHelper(this);
        // Initialize the IRCClient instance
        this.ircClient = IRCClient.getInstance();


        displayMessagesForUser(currentChatUser);


        // Initialize UI components
        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);



        // Retrieve the chat history for the target user

        List<String> chatHistory = GlobalMessageListener.getInstance().getPrivateMessagesFor(targetUser);


        for (String historyMessage : chatHistory) {
            parsePrivateMessage(historyMessage);
        }



        // Initialize Drawer components
        drawerLayout = findViewById(R.id.drawer_layout);
        btnOpenLeftDrawer = findViewById(R.id.btnOpenLeftDrawer);
        chatsListView = findViewById(R.id.chatsListView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        chatsListView.setAdapter(adapter);




        // Initialize ChatHelper
        chatHelper = new ChatHelper(this);
        chatHelper.setChatTextView(chatTextView);

        // Initialize DrawerLeft
        drawerLeft = new DrawerLeft(this, chatsListView, btnOpenLeftDrawer, drawerLayout, chatHelper);


        // Set up listeners for private messages
        setupPrivateMessageListeners();

        // Set OnClick Listener for Drawer Button
        btnOpenLeftDrawer.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

    }

    private void setupPrivateMessageListeners() {
        // Set up the send message button listener for private messages
        sendMessageButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                String currentUsername = (ircClient != null) ? ircClient.getNickname() : "DefaultUsername";
                chatHelper.sendMessage(targetUser, message);

                // Store the sent message
                GlobalMessageListener.getInstance().addPrivateMessage(currentUsername, targetUser, message);

                messageEditText.setText("");
            }
        });

        // Set up the private message listener
        chatHelper.setPrivateMessageListener((sender, message) -> {
            // Check if the sender of the incoming message matches the targetUser
            if (!sender.equals(targetUser)) {
                // This message is not from the current chat user, so ignore it
                return;
            }

            // Display the message if it's from the current chat user
            chatTextView.append(sender + ": " + message + "\n");

            // Store the received private message
            GlobalMessageListener.getInstance().addPrivateMessage(sender, targetUser, message);
            List<String> chatContent = SharedDataSource.getInstance().getStoredMessages(targetUser);

            // Update the UI to display the loaded chat content
            adapter.addAll(chatContent);
        });
    }



    private void parsePrivateMessage(String retrievedMessage) {
        String[] parts = retrievedMessage.split(": ", 2);
        if (parts.length > 1) {
            String sender = parts[0].trim();
            String messageContent = parts[1].trim();
            chatTextView.append(sender + ": " + messageContent + "\n");
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        targetUser = intent.getStringExtra("chatTarget");

        // Clear the adapter to avoid duplication
        // Load the chat history for the new targetUser
        List<String> chatContent = GlobalMessageListener.getInstance().getPrivateMessagesFor(targetUser);
        for (String historyMessage : chatContent) {
            parsePrivateMessage(historyMessage);
        }

        displayMessagesForUser(currentChatUser);

        // Update the UI to display the loaded chat content
        adapter.clear();
        adapter.addAll(chatContent);
        adapter.notifyDataSetChanged();
    }




    @Override
    protected void onResume() {
        super.onResume();


        // Clear the adapter to avoid duplication
        adapter.clear();

        // Get the chat history for the target user
        List<String> chatContent = SharedDataSource.getInstance().getStoredMessages(targetUser);

        // Add your own messages to the chat history
        String currentUsername = (ircClient != null) ? ircClient.getNickname() : "DefaultUsername";
        chatContent.addAll(SharedDataSource.getInstance().getStoredMessages(currentUsername));

        // Sort the chat content if needed (based on timestamp or other criteria)

        // Update the UI to display the loaded chat content
        adapter.addAll(chatContent);
        adapter.notifyDataSetChanged();

        displayMessagesForUser(currentChatUser);
    }

    void displayMessagesForUser(String targetUser) {
        List<String> allMessagesStrings = SharedDataSource.getInstance().getStoredMessages(targetUser);
        ArrayList<MessageFilter> allMessages = new ArrayList<>();

        for (String messageString : allMessagesStrings) {
            if (messageString != null) {
                String[] parts = messageString.split(":");
                if (parts.length >= 3) { // Ensure the messageString has the expected format
                    String sender = parts[0];
                    String recipient = parts[1];

                    allMessages.add(new MessageFilter(sender, recipient));
                }
            }
        }

        ArrayList<MessageFilter> filteredMessages = new ArrayList<>();

        for (MessageFilter message : allMessages) {
            if (message.getSender().equals(targetUser) || message.getRecipient().equals(targetUser)) {
                filteredMessages.add(message);
            }
        }

        // Now, use the filteredMessages list to display in the chat UI
    }








    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatHelper.onDestroy();
    }
}