package com.example.sakilmallick.offlinehelper;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.shashank.sony.fancytoastlib.FancyToast;

public class TextQueryActivity extends AppCompatActivity {

    public static final String
            ACTION_QUERY_BROADCAST = TextQueryActivity.class.getName() + "QueryBroadcast",
            EXTRA_QUERY = "extra_source";
    String travel_mode;

    // UI elements
    EditText queryEditText;
    TextView answersTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_query);


        // Layout initialisation
        queryEditText = findViewById(R.id.queryEditText);
        answersTextView = findViewById(R.id.answersTextView);
        answersTextView.setText("");

        // Receive answers sent from TextQueryService using Broadcast Manager
        // And display them
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String answers = intent.getStringExtra(TextQueryService.EXTRA_ANSWER);
                        answersTextView.append(answers + "\n");
                    }
                }, new IntentFilter(TextQueryService.ACTION_ANSWER_BROADCAST)
        );

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Boolean signal = intent.getBooleanExtra(TextQueryService.EXTRA_SIGNAL, false);
                        if (signal) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(TextQueryActivity.this);
                            builder.setTitle("Internet required to start service");
                            builder.setMessage("Internet connection is required for the first time the service starts for user verification." +
                                    " Connect to the Internet and then start the service again.");
                            builder.setNeutralButton("OK", (dialog, which) -> {
                                dialog.cancel();
                            });
                            builder.show();
                        }
                    }
                }, new IntentFilter(TextQueryService.ACTION_SIGNAL_BROADCAST)
        );
    }

    public void startService(View view) {
        if (!isServiceRunning()) {
            Intent serviceIntent = new Intent(this, TextQueryService.class);
            startService(serviceIntent);
        } else {
            FancyToast.makeText(getApplicationContext(),
                    "Service already running",
                    FancyToast.LENGTH_SHORT, FancyToast.WARNING, false)
                    .show();
        }
    }

    public void prepareMessage(View view) {
        if (!(TextUtils.isEmpty(queryEditText.getText()))) {
            if (isServiceRunning()) {
                String query_text = queryEditText.getText().toString();
                sendBroadcastMessage(query_text);
            } else {
                FancyToast.makeText(getApplicationContext(),
                        "Start the service first",
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                        .show();
            }
        } else {
            FancyToast.makeText(getApplicationContext(),
                    "Enter query",
                    FancyToast.LENGTH_LONG, FancyToast.CONFUSING, false)
                    .show();
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (HelperService.class.getName().equals(service.service.getClassName())
                    || TextQueryService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void sendBroadcastMessage(String query_text) {
        if (!query_text.equals("")) {
            Intent intent = new Intent(ACTION_QUERY_BROADCAST);
            intent.putExtra(EXTRA_QUERY, query_text);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}