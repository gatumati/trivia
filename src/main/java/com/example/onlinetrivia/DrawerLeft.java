package com.example.onlinetrivia;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

public class DrawerLeft {
    private Activity activity;
    private Context context;
    private ListView chatsListView;
    private Button btnOpenLeftDrawer;
    private DrawerLayout drawerLayout;
    private IRCClient ircClient;
    private ChatHelper chatHelper; // Added reference to ChatHelper
    private List<String> connectedChannels = new ArrayList<>();

    public DrawerLeft(Activity activity, ListView chatsListView, Button btnOpenLeftDrawer, DrawerLayout drawerLayout, ChatHelper chatHelper) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.chatsListView = chatsListView;
        this.btnOpenLeftDrawer = btnOpenLeftDrawer;
        this.drawerLayout = drawerLayout;
        this.ircClient = IRCClient.getInstance();
        this.chatHelper = chatHelper; // Initialize the ChatHelper
        initializeListeners();

        // Register as a listener to get notified when a private message is received
        GlobalMessageListener.getInstance().addPrivateMessageListener(this::updateConnectedChannelsList);

        // Set up the click listener for the chatsListView
        setupChatsListViewClickListener();
    }

    private void initializeListeners() {
        btnOpenLeftDrawer.setOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);
            refreshDrawer();
        });

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerView.findViewById(R.id.chatsListView) != null) {
                    btnOpenLeftDrawer.setVisibility(View.GONE);
                    updateConnectedChannelsList();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                btnOpenLeftDrawer.setVisibility(View.VISIBLE);
            }
        });
    }

    public void refreshDrawer() {
        List<String> names = GlobalMessageListener.getInstance().getNamesList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, names);
        chatsListView.setAdapter(adapter);
    }

    public void updateConnectedChannelsList() {
        connectedChannels = ircClient.getCurrentChatList();
        List<String> privateChats = GlobalMessageListener.getInstance().getPrivateChats();
        connectedChannels.addAll(privateChats);
        activity.runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, connectedChannels);
            chatsListView.setAdapter(adapter);
        });
    }

    private void setupChatsListViewClickListener() {
        chatsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);

        });
    }

    // Ensure to unregister the listener when the activity is destroyed to prevent memory leaks
    public void onDestroy() {
        GlobalMessageListener.getInstance().removePrivateMessageListener(this::updateConnectedChannelsList);
    }
}
