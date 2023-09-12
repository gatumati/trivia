package com.example.onlinetrivia;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class ChannelActivity extends Activity {

    private boolean doubleBackToExitPressedOnce = false;
    private ChatHelper chatHelper;
    private TextView chatTextView;
    private EditText messageEditText;
    private Button sendMessageButton, btnOpenRightDrawer, btnOpenLeftDrawer;
    private ListView namesListView, chatsListView;
    private DrawerLayout drawerLayout;
    private String channelName;
    private Toast exitToast;
    private DrawerLeft drawerLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        chatHelper = new ChatHelper(this);

        if (getIntent().hasExtra("channel_name")) {
            channelName = getIntent().getStringExtra("channel_name");
            setTitle(channelName);
        }
        chatHelper.setChannelName(channelName);


        initializeComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatHelper.initializeListeners();
        chatHelper.displayChatHistory(chatTextView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatHelper.onDestroy();
    }

    private void initializeComponents() {
        // Initialize views
        chatTextView = findViewById(R.id.chatTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        btnOpenRightDrawer = findViewById(R.id.btnOpenDrawer);
        btnOpenLeftDrawer = findViewById(R.id.btnOpenLeftDrawer);
        namesListView = findViewById(R.id.namesListView);
        chatsListView = findViewById(R.id.chatsListView);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set views in ChatHelper
        chatHelper.setChatTextView(chatTextView);
        chatHelper.setNamesListView(namesListView);
        chatHelper.setChatsListView(chatsListView);
        chatHelper.setDrawerButtons(btnOpenRightDrawer, btnOpenLeftDrawer);

        // Set listeners
        if (chatsListView != null) {
            chatsListView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedItem = (String) parent.getItemAtPosition(position);
                chatHelper.handleChatsItemClick(selectedItem);
            });
        } else {
            Log.e("ChannelActivity", "chatsListView is null!");
        }

        DrawerRight drawerRight = new DrawerRight(this, namesListView, btnOpenRightDrawer, drawerLayout, chatHelper);

        drawerLeft = new DrawerLeft(this, chatsListView, btnOpenLeftDrawer, drawerLayout);



        sendMessageButton.setOnClickListener(view -> {
            String message = messageEditText.getText().toString();
            chatHelper.handleSendMessage(channelName, message);
            messageEditText.setText("");
        });

        chatHelper.setChannelMessageListener((channel, sender, message) -> {
            if (channel.equals(channelName)) {
                runOnUiThread(() -> {
                    String displayMessage = chatHelper.updateChatDisplay(sender + ": " + message);
                    chatTextView.append(displayMessage + "\n");
                });
            }
        });

        btnOpenRightDrawer.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.END));
        btnOpenLeftDrawer.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
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

    public String getChannelName() {
        return channelName;
    }




}