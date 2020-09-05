package com.example.sakilmallick.offlinehelper;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bridgefy.sdk.client.BFEnergyProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.BridgefyClient;
import com.bridgefy.sdk.client.Config;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.bridgefy.sdk.client.RegistrationListener;
import com.bridgefy.sdk.client.StateListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.HashMap;

import static com.example.sakilmallick.offlinehelper.App.ANSWER_CHANNEL_ID;
import static com.example.sakilmallick.offlinehelper.App.FOREGROUND_CHANNEL_ID;
import static com.example.sakilmallick.offlinehelper.App.QUERY_CHANNEL_ID;
import static com.example.sakilmallick.offlinehelper.App.manager;
import static com.example.sakilmallick.offlinehelper.Constants.BRIDGEFY_API_KEY;
import static com.example.sakilmallick.offlinehelper.Constants.TAG;

public class HelperService extends Service {

    public static final String
            ACTION_ANSWER_BROADCAST = HelperService.class.getName() + "AnswerBroadcast",
            EXTRA_ANSWER = "extra_answer";
    public static final String ACTION_SIGNAL_BROADCAST = HelperService.class.getName() + "SignalBroadcast",
            EXTRA_SIGNAL = "extra_signal";
    int notificationId;

    StateListener stateListener = new StateListener() {
        @Override
        public void onStarted() {
            Log.i(TAG, "onStarted: Bridgefy started");
        }

        @Override
        public void onStartError(String s, int i) {
            switch (i) {
                case (StateListener.LOCATION_SERVICES_DISABLED):
                    //location in the device has been disabled
                    break;
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        stopSelf();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }
    MessageListener messageListener = new MessageListener() {
        @Override
        public void onBroadcastMessageReceived(Message message) {
            if (isOnline()) {
                FancyToast.makeText(getApplicationContext(),
                        "New request received",
                        FancyToast.LENGTH_SHORT, FancyToast.INFO, false)
                        .show();
                // Increment notification ID
                notificationId++;

                HashMap content = message.getContent();

                String source = content.get(Constants.source_key).toString();
                String destination = content.get(Constants.destination_key).toString();
                String travel_mode = content.get(Constants.travel_mode_key).toString();

                // Create an explicit intent for an Activity in your app
                Intent intent = new Intent(getApplicationContext(), HelperActivity.class);
                // Putting information in intent to get in the activity
                intent.putExtra("sender_id", message.getSenderId());
                intent.putExtra(Constants.source_key, source);
                intent.putExtra(Constants.destination_key, destination);
                intent.putExtra(Constants.travel_mode_key, travel_mode);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), QUERY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Direction Request")
                        .setContentText("Source: " + source + "\n" + "Destination: " + destination + "\n" + "Travel Mode: " + travel_mode)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        // Set the intent that will fire when the user taps the notification
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(notificationId, mBuilder.build());
            }
        }

        @Override
        public void onMessageReceived(Message message) {
            FancyToast.makeText(getApplicationContext(),
                    "Answer received",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false)
                    .show();

            // Increment notification ID
            notificationId++;

            HashMap content = message.getContent();
            String answer_text = content.get(Constants.answer).toString();

            Intent intent = new Intent(getApplicationContext(), AnswerActivity.class);
            // Putting information in intent to get in the activity
            intent.putExtra("route", "Direction from " + content.get(Constants.source_key)
                    + " to " + content.get(Constants.destination_key));
            intent.putExtra("answer", answer_text);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), ANSWER_CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle("Direction for: " + "\n" + content.get(Constants.source_key)
                            + "\n" + " to " + content.get(Constants.destination_key))
                    .setContentText("Sent from: " + content.get(Constants.device_name))
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(answer_text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(notificationId, mBuilder.build());

            // Send the answer to MainActivity
            sendBroadcastMessage(answer_text);
        }
    };

    private void sendBroadcastMessage(String cur_answer) {
        if (cur_answer != null) {
            Intent intent = new Intent(ACTION_ANSWER_BROADCAST);
            intent.putExtra(EXTRA_ANSWER, cur_answer);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        notificationId = 1;

        String ACTION_STOP_SERVICE = "Stop Service";
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            Log.d(TAG, "called to cancel service");
            // Always stop Bridgefy when it's no longer necessary
            Bridgefy.stop();
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.disable();
            manager.cancel(1);
            stopSelf();
        }

        Intent stopSelf = new Intent(this, HelperService.class);
        stopSelf.setAction(ACTION_STOP_SERVICE);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getAppName())
                .setContentText(getAppName() + " is running in the background")
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .addAction(R.mipmap.ic_launcher, "STOP", pStopSelf)
                .build();

        startForeground(1, notification);

        // Things to do
        //Enable bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        //Always use the Application context to avoid leaks
        Bridgefy.initialize(getApplicationContext(), BRIDGEFY_API_KEY, new RegistrationListener() {
            @Override
            public void onRegistrationSuccessful(BridgefyClient bridgefyClient) {
                // Important data can be fetched from the BridgefyClient object
                // Bridgefy is ready to start
                Log.i(TAG, "Bridgefy registration successful");
                Config.Builder builder = new Config.Builder();
                builder.setEnergyProfile(BFEnergyProfile.HIGH_PERFORMANCE);
                builder.setEncryption(false);

                Bridgefy.start(messageListener, stateListener, builder.build());
            }

            @Override
            public void onRegistrationFailed(int errorCode, String message) {
                // Something went wrong: handle error code, maybe print the message
                Log.e(TAG, "Bridgefy registration failed");
                if (!isOnline()) {
                    sendBroadcastSignal();
                }
                stopSelf();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String source = intent.getStringExtra(MainActivity.EXTRA_SOURCE);
                        String destination = intent.getStringExtra(MainActivity.EXTRA_DESTINATION);
                        String travel_mode = intent.getStringExtra(MainActivity.EXTRA_TRAVEL_MODE);
                        //Assemble the data that we are about to send
                        HashMap<String, Object> data = new HashMap<>();
                        data.put(Constants.source_key, source);
                        data.put(Constants.destination_key, destination);
                        data.put(Constants.travel_mode_key, travel_mode);
                        data.put(Constants.date_sent, Double.parseDouble("" + System.currentTimeMillis()));
                        data.put(Constants.device_name, Build.MANUFACTURER + " " + Build.MODEL);
                        // Send broadcast message
                        //Message message = Bridgefy.createMessage(data);
                        Message message = new Message.Builder().setContent(data).build();

                        //Broadcast messages are sent to anyone that can receive it
                        Bridgefy.sendBroadcastMessage(message);
                    }
                }, new IntentFilter(MainActivity.ACTION_QUERY_BROADCAST)
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String sender_id = intent.getStringExtra(HelperActivity.EXTRA_SENDER_ID);
                        String source = intent.getStringExtra(HelperActivity.EXTRA_SOURCE);
                        String destination = intent.getStringExtra(HelperActivity.EXTRA_DESTINATION);
                        String answer_text = intent.getStringExtra(HelperActivity.EXTRA_HELP_ANSWER);
                        //Assemble the data that we are about to send
                        HashMap<String, Object> data = new HashMap<>();
                        data.put(Constants.source_key, source);
                        data.put(Constants.destination_key, destination);
                        data.put(Constants.answer, answer_text);
                        data.put(Constants.date_sent, Double.parseDouble("" + System.currentTimeMillis()));
                        data.put(Constants.device_name, Build.MANUFACTURER + " " + Build.MODEL);

                        // Create a message with the HashMap and the recipient's id
                        Message message = new Message.Builder().setContent(data).setReceiverId(sender_id).build();

                        // Send the message to the specified recipient
                        Bridgefy.sendMessage(message);
                    }
                }, new IntentFilter(HelperActivity.ACTION_HELP_BROADCAST)
        );

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        return START_STICKY;
    }

    private void sendBroadcastSignal() {
        Intent intent = new Intent(ACTION_SIGNAL_BROADCAST);
        intent.putExtra(EXTRA_SIGNAL, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String getAppName() {
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(this.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister broadcast listeners
        unregisterReceiver(mReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
