package com.example.sakilmallick.offlinehelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class AnswerActivity extends AppCompatActivity {

    TextView answersTextView;
    TextView routeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        answersTextView = findViewById(R.id.answersTextView);
        routeTextView = findViewById(R.id.routeTextView);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String route = bundle.getString("route");
            String directions = bundle.getString("answer");
            routeTextView.setText(route);
            answersTextView.setText(directions);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishAndRemoveTask();
    }
}
