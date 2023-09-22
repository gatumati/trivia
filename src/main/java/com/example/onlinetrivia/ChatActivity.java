package com.example.onlinetrivia;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class ChatActivity extends AppCompatActivity {

    private TextView chatTextView;
    private EditText messageEditText;
    private Button sendMessageButton, btnOpenLeftDrawer;
    private ChatHelper chatHelper;
    private DrawerLeft drawerLeft;
    private ListView chatsListView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize UI components
        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        // Initialize Drawer components
        drawerLayout = findViewById(R.id.drawer_layout);
        btnOpenLeftDrawer = findViewById(R.id.btnOpenLeftDrawer);
        chatsListView = findViewById(R.id.chatsListView);


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
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatHelper.onDestroy();
    }
}
