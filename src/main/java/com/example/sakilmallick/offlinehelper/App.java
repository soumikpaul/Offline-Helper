package com.example.sakilmallick.offlinehelper;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String FOREGROUND_CHANNEL_ID = "foreground_channel";
    public static final String QUERY_CHANNEL_ID = "query_channel";
    public static final String ANSWER_CHANNEL_ID = "answer_channel";

    public static NotificationManager manager;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel foreground_channel = new NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    "Foreground Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            foreground_channel.setDescription("This is Channel for Foreground service notification");
            NotificationChannel query_channel = new NotificationChannel(
                    QUERY_CHANNEL_ID,
                    "Query Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            query_channel.setDescription("This is Channel for Query");

            NotificationChannel answer_channel = new NotificationChannel(
                    ANSWER_CHANNEL_ID,
                    "Answer Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            answer_channel.setDescription("This is Channel for Answer");

            manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(foreground_channel);
            manager.createNotificationChannel(query_channel);
            manager.createNotificationChannel(answer_channel);
        }
    }
}
