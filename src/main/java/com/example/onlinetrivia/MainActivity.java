package com.example.onlinetrivia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView serverResponseTextView;
    private final String serverAddress = "irc.undernet.org";
    private final int serverPort = 6667;
    private String nickname = generateGuestNickname();  // Assign a generated guest nickname
    private final String username = "Online Trivia Player";
    private final String ident = "Trivia";
    private IRCClient ircClient;
    private static final int NAME_ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        // Set app to fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        serverResponseTextView = findViewById(R.id.serverResponseTextView);

        // Initialize IRC client and start the connection
        ircClient = IRCClient.getInstance();
        ircClient.initialize(this, serverAddress, serverPort, username, nickname, ident);
        new Thread(ircClient).start();

        // Open NameActivity after a connection has been established
        ircClient.setOnConnectionEstablishedListener(() -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, NameActivity.class);
                intent.putExtra("current_nickname", nickname);
                startActivityForResult(intent, NAME_ACTIVITY_REQUEST_CODE);
            }, 10000);
        });

        // Set immersive mode
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check for results from NameActivity and update nickname
        if (requestCode == NAME_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            String newNickname = data.getStringExtra("new_nickname");
            if (newNickname != null && !newNickname.isEmpty()) {
                nickname = newNickname;
                ircClient.changeNickname(newNickname);

                // Launch ChannelListActivity
                Intent intent = new Intent(MainActivity.this, ChannelListActivity.class);
                startActivity(intent);
            }
        }
    }

    // Update UI with the server's response
    public void updateServerResponse(final String response) {
        runOnUiThread(() -> serverResponseTextView.append(response + "\n"));
    }

    // Generate a nickname in the format "guestXXXXX"
    private String generateGuestNickname() {
        Random rand = new Random();
        int randomNum = rand.nextInt(99999);
        return "guest" + String.format("%05d", randomNum);
    }
}
