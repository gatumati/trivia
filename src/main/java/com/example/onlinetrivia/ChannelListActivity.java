package com.example.onlinetrivia;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelListActivity extends Activity {

    private IRCClient ircClient; // Reference to your IRCClient instance
    private List<String> combinedList;  // Reference to the combinedList from DrawerLeft

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list); // Assuming you have a layout named activity_channel_list

        // Get the instance of IRCClient
        ircClient = IRCClient.getInstance();

        // Initialize the combinedList
        combinedList = new ArrayList<>();

        showJoinChannelPopup();
    }

    private void showJoinChannelPopup() {
        Dialog joinChannelDialog = new Dialog(this);
        joinChannelDialog.setContentView(R.layout.activity_channel_list);

        LinearLayout channelButtonsLayout = joinChannelDialog.findViewById(R.id.channelButtonsLayout);

        List<String> channels = Arrays.asList("#cutitas", "#Gratis", "#Trivia"); // You can expand this list

        for (String channel : channels) {
            Button channelButton = new Button(this);
            channelButton.setText(channel);
            channelButton.setOnClickListener(view -> {
                // Handle channel join here
                if (ircClient != null) {
                    ircClient.joinChannel(channel);
                    // Add the channel to the combinedList
                    combinedList.add(channel);
                    SharedDataSource.getInstance().setCombinedList(combinedList);
                    Log.d("ChannelListActivity", "combinedList updated: " + combinedList.toString());

                    // Start the ChannelActivity once a channel is joined
                    Intent intent = new Intent(ChannelListActivity.this, ChannelActivity.class);
                    intent.putExtra("channel_name", channel);
                    startActivity(intent);
                    joinChannelDialog.dismiss();
                    finish();  // Close ChannelListActivity after starting ChannelActivity
                }
            });
            channelButtonsLayout.addView(channelButton);
        }


        joinChannelDialog.setOnDismissListener(dialog -> finish());  // Close the activity if the dialog is dismissed by any means
        joinChannelDialog.show();
    }
}
