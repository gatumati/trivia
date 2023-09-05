package com.example.onlinetrivia;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public class ChannelListActivity extends Activity {

    private IRCClient ircClient; // Reference to your IRCClient instance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the instance of IRCClient
        ircClient = IRCClient.getInstance();

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

        Button btnClose = joinChannelDialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(view -> {
            joinChannelDialog.dismiss();
            finish();  // Close the activity after closing the dialog
        });

        joinChannelDialog.setOnDismissListener(dialog -> finish());  // Close the activity if the dialog is dismissed by any means
        joinChannelDialog.show();
    }
}
