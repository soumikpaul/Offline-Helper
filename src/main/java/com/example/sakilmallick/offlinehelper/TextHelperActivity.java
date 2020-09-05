package com.example.sakilmallick.offlinehelper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.shashank.sony.fancytoastlib.FancyToast;

import static com.example.sakilmallick.offlinehelper.Constants.query;

public class TextHelperActivity extends AppCompatActivity {

    public static final String
            ACTION_HELP_BROADCAST = TextHelperActivity.class.getName() + "HelpBroadcast",
            EXTRA_QUERY = "extra_query",
            EXTRA_HELP_ANSWER = "extra_help_answer",
            EXTRA_SENDER_ID = "extra_sender_id";

    String sender_id;
    String query_text;
    String answer;

    TextView queryTextView;
    EditText answerEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_helper);

        // Layout initialisation
        queryTextView = findViewById(R.id.queryTextView);
        answerEditText = findViewById(R.id.answerEditText);
        queryTextView.setMovementMethod(new ScrollingMovementMethod());

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        sender_id = bundle.getString("sender_id");
        query_text = bundle.getString(query);

        queryTextView.setText(query_text);
    }

    public void prepareMessage(View view) {
        if (isServiceRunning()) {
            sendBroadcastMessage();
            finishAndRemoveTask();
        } else {
            Intent serviceIntent = new Intent(this, TextQueryService.class);
            startService(serviceIntent);
            FancyToast.makeText(getApplicationContext(),
                    "Service was stopped\nNow it's turned ON\nYou can send the answer now",
                    FancyToast.LENGTH_LONG, FancyToast.INFO, false)
                    .show();
        }
    }

    private void sendBroadcastMessage() {
        if (!(TextUtils.isEmpty(answerEditText.getText()))) {
            answer = answerEditText.getText().toString();
            Intent intent = new Intent(ACTION_HELP_BROADCAST);
            intent.putExtra(EXTRA_SENDER_ID, sender_id);
            intent.putExtra(EXTRA_QUERY, query_text);
            intent.putExtra(EXTRA_HELP_ANSWER, answer);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            FancyToast.makeText(getApplicationContext(),
                    "Enter an answer please",
                    FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                    .show();
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TextQueryService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAndRemoveTask();
    }
}
