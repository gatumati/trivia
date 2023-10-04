package com.example.onlinetrivia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.Collections;
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

    private List<String> combinedList = new ArrayList<>();
    private List<String> channels = new ArrayList<>();

    private Handler mainHandler = new Handler(Looper.getMainLooper());


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
        });
        GlobalMessageListener.getInstance().setOnChatUpdateListener(() -> {
        });



        ChatHelper chatHelper = ChatHelper.getInstance();
        chatHelper.setOnDrawerListRefreshRequestedListener(() -> {
            refreshDrawerList();
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




    public void updateConnectedChannelsList() {
        connectedChannels = ircClient.getCurrentChatList();
        List<String> privateChats = GlobalMessageListener.getInstance().getPrivateChats();
        connectedChannels.addAll(privateChats);
        activity.runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, connectedChannels);
            chatsListView.setAdapter(adapter);

        });
    }

    // Inside DrawerLeft class
    private void setupChatsListViewClickListener() {
        chatsListView.setOnItemClickListener((parent, view, position, id) -> {
            List<String> combinedList = SharedDataSource.getInstance().getCombinedList();
            if (position < combinedList.size()) {
                String selectedItem = combinedList.get(position);
                Intent intent;
                if (selectedItem.startsWith("#")) {
                    // It's a channel
                    intent = new Intent(context, ChannelActivity.class);
                    intent.putExtra("channel_name", selectedItem);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                } else {
                    // It's a private chat
                    intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("chatTarget", selectedItem);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                }
                activity.getApplicationContext().startActivity(intent);
            } else {
                Log.e("DrawerLeft", "Invalid position clicked: " + position);
            }
        });
    }






    public void refreshDrawerList() {
        List<String> channelsList = ChatHelper.getInstance().getConnectedChannels();
        List<String> privateChats = ChatHelper.getInstance().getActivePrivateChats();

        Collections.sort(channelsList);
        Collections.sort(privateChats);
        combinedList.addAll(channelsList);
        combinedList.addAll(privateChats);
        activity.runOnUiThread(() -> {
            Log.d("DrawerLeft", "Combined List: " + combinedList);

        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, combinedList);
        chatsListView.setAdapter(adapter);
        refreshDrawerList();
    }



    // Ensure to unregister the listener when the activity is destroyed to prevent memory leaks
    public void onDestroy() {
        GlobalMessageListener.getInstance().removePrivateMessageListener(this::updateConnectedChannelsList);
    }
}
