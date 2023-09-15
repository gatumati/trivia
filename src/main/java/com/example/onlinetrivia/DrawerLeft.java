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
    private List<String> connectedChannels = new ArrayList<>();
    private List<String> channels = new ArrayList<>();
    private List<String> privateChats = new ArrayList<>();

    public DrawerLeft(Activity activity, ListView chatsListView, Button btnOpenLeftDrawer, DrawerLayout drawerLayout) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.chatsListView = chatsListView;
        this.btnOpenLeftDrawer = btnOpenLeftDrawer;
        this.drawerLayout = drawerLayout;
        this.ircClient = IRCClient.getInstance();
        initializeListeners();
    }


    private void initializeListeners() {
        btnOpenLeftDrawer.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerView.findViewById(R.id.chatsListView) != null) {
                    btnOpenLeftDrawer.setVisibility(View.GONE);
                    updateConnectedChannelsList();  // Update the list when the drawer is opened
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                btnOpenLeftDrawer.setVisibility(View.VISIBLE);
            }
        });
    }



    public void updateConnectedChannelsList() {
        connectedChannels = ircClient.getCurrentChatList(); // Fetch the list from ircClient
        activity.runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, connectedChannels);
            chatsListView.setAdapter(adapter);
        });
    }
}