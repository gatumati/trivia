package com.example.onlinetrivia;

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

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private TextView chatTextView;
    private EditText messageEditText;
    private Button sendMessageButton, btnOpenLeftDrawer;
    private ChatHelper chatHelper;
    private DrawerLeft drawerLeft;
    private ListView chatsListView;
    private DrawerLayout drawerLayout;

    private IRCClient ircClient;

    private ArrayAdapter<String> adapter;

    private String targetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ChatActivity", "onResume called for user: " + targetUser);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatHelper = new ChatHelper(this);


        // Initialize UI components
        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        if (targetUser != null) {
            // Use the targetUser variable
        } else {
            Log.e("ChatActivity", "targetUser is null");
        }
        targetUser = getIntent().getStringExtra("chatTarget");

        final String targetUser = getIntent().getStringExtra("chatTarget");


        // Retrieve the chat history for the target user
        List<String> chatHistory = GlobalMessageListener.getInstance().getPrivateMessagesFor(targetUser);

        // Display the chat history
        for (String historyMessage : chatHistory) {
            chatTextView.append(historyMessage + "\n");
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
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Assuming the target user for the private message is passed as an intent extra
                    String targetUser = getIntent().getStringExtra("chatTarget");
                    chatHelper.sendMessage(targetUser, message);
                    messageEditText.setText("");
                }
            }
        });

        // Set up the private message listener
        chatHelper.setPrivateMessageListener(new ChatHelper.OnPrivateMessageReceivedListener() {
            @Override
            public void onPrivateMessageReceived(String sender, String message) {
                chatTextView.append(sender + ": " + message + "\n");

                /// Store the received private message
                GlobalMessageListener.getInstance().addPrivateMessage(sender, targetUser, message);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String activePrivateChatUser = SharedDataSource.getInstance().getActivePrivateChatUser();
        List<String> chatContent = SharedDataSource.getInstance().getStoredMessages(activePrivateChatUser);

        // Update the UI to display the loaded chat content
        // For example, if you're using an ArrayAdapter:
        adapter.addAll(chatContent);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatHelper.onDestroy();
    }
}
