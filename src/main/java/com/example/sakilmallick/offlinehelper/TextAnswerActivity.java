package com.example.sakilmallick.offlinehelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class TextAnswerActivity extends AppCompatActivity {

    TextView answersTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_answer);

        answersTextView = findViewById(R.id.answersTextView);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String query_text = bundle.getString("query");
            String answer_text = bundle.getString("answer");
            String device = bundle.getString("device");
            answersTextView.setText("Q: " + query_text + "\n" + "A: " + answer_text + "\n" + "Sent from: " + device);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAndRemoveTask();
    }
}
