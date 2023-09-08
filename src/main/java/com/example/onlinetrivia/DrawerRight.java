package com.example.onlinetrivia;

import android.content.Context;
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

public class DrawerRight {
    public void refreshNamesList() {
        ircClient.setNamesListener(this::updateNamesList);
        new Thread(() -> {
            ircClient.requestNamesForChannel(activity.getChannelName());
        }).start();
    }
    private ChannelActivity activity;

    private Context context;
    private ListView namesListView;
    private IRCClient ircClient;
    private Button btnOpenRightDrawer;
    private DrawerLayout drawerLayout;
    private List<String> currentNames = new ArrayList<>();




    public DrawerRight(ChannelActivity activity, ListView namesListView, Button btnOpenRightDrawer, DrawerLayout drawerLayout) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.namesListView = namesListView;
        this.btnOpenRightDrawer = btnOpenRightDrawer;
        this.drawerLayout = drawerLayout;
        this.ircClient = IRCClient.getInstance();
        initializeListeners();
        setupDrawerButton();
    }

    private void initializeListeners() {
        Log.d("DrawerRight", "Initializing listeners");
        ircClient.setNamesListener(this::updateNamesList);
    }

    private void setupDrawerButton() {
        btnOpenRightDrawer.setOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.END);
            refreshNamesList();
        });
        Log.d("DrawerRight", "Right drawer opened");

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {


            @Override
            public void onDrawerOpened(View drawerView) {
                Log.d("DrawerRight", "Drawer opened");

                if (drawerView.findViewById(R.id.namesListView) != null) {
                    btnOpenRightDrawer.setVisibility(View.GONE);
                    namesListView.invalidate();
                    refreshNamesList();

                    Log.d("DrawerRight", "Fetching channel name: " + activity.getChannelName());

                }
            }


            @Override
            public void onDrawerClosed(View drawerView) {
                Log.d("DrawerRight", "Drawer closed");

                if (drawerView.findViewById(R.id.namesListView) != null) {
                    btnOpenRightDrawer.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    public void updateNamesList(List<String> names) {
        Log.d("DrawerRight", "Updating names list with size: " + names.size());

        activity.runOnUiThread(() -> {
            Collections.sort(names, (o1, o2) -> {
                if (o1.startsWith("@") && !o2.startsWith("@")) {
                    return -1;
                } else if (!o1.startsWith("@") && o2.startsWith("@")) {
                    return 1;
                }
                return o1.compareToIgnoreCase(o2);
            });
            currentNames.addAll(names);  // Add the new names
            currentNames.clear();  // Clear the current names
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, names);
            namesListView.setAdapter(adapter);
            namesListView.invalidate();
            Log.d("DrawerRight", "Names list updated and invalidated");

        });
    }

}
