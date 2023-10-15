package com.example.onlinetrivia;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;



public class MessageService extends Service {

    private static final String CHANNEL_ID = "MessageServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Handle the incoming message
        String message = intent.getStringExtra("message");
        String sender = intent.getStringExtra("sender");

        // Update SharedDataSource or any other in-memory data
        SharedDataSource.getInstance().storeMessage(sender, message);

        // Show a notification to the user
        showNotification(sender, message);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Message Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String sender, String message) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatTarget", sender);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("New message from " + sender)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_message)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

    }
}
