package com.example.sakilmallick.offlinehelper;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.example.sakilmallick.offlinehelper.Constants.DIRECTIONS_API_KEY;
import static com.example.sakilmallick.offlinehelper.Constants.TAG;
import static com.example.sakilmallick.offlinehelper.Constants.destination_key;
import static com.example.sakilmallick.offlinehelper.Constants.source_key;
import static com.example.sakilmallick.offlinehelper.Constants.travel_mode_key;

public class HelperActivity extends AppCompatActivity {

    public static final String
            ACTION_HELP_BROADCAST = HelperActivity.class.getName() + "HelpBroadcast",
            EXTRA_SOURCE = "extra_source",
            EXTRA_DESTINATION = "extra_destination",
            EXTRA_HELP_ANSWER = "extra_help_answer",
            EXTRA_SENDER_ID = "extra_sender_id";
    String source;

    String sender_id;
    String destination;
    String travel_mode;
    String directions;
    TextView routeTextView;

    TextView queryTextView;
    RequestQueue mRequestQueue;
    private int numberOfRequestsToMake = 1;

    int FLAG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);

        // Initialize the progressdialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting to Google Maps Direction API");

        // Layout initialisation
        routeTextView = findViewById(R.id.routeTextView);
        queryTextView = findViewById(R.id.queryTextView);
        //queryTextView.setMovementMethod(new ScrollingMovementMethod());

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        sender_id = bundle.getString("sender_id");
        source = bundle.getString(source_key);
        destination = bundle.getString(destination_key);
        travel_mode = bundle.getString(travel_mode_key);

        routeTextView.setText("Directions from " + source + " to " + destination);

        // Instantiate the RequestQueue.
        mRequestQueue = Volley.newRequestQueue(this);
        String url;

        url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + source
                + "&destination=" + destination
                + "&mode=" + travel_mode
                + "&key=" + DIRECTIONS_API_KEY;

        // Show the progressdialog just before making a request
        progressDialog.show();

        StringBuilder dirStrBuilder = new StringBuilder("");
        // Request a JSONObject response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Things to do on HTTP request response
                        try {
                            String status = response.getString("status");
                            switch (status) {
                                case "OK": {
                                    JSONObject route = (JSONObject) response.getJSONArray("routes").get(0);
                                    JSONArray legs = route.getJSONArray("legs");
                                    JSONObject leg = (JSONObject) legs.get(0);
                                    JSONObject arrival_time = (JSONObject) leg.get("arrival_time");
                                    String arrival_time_text = arrival_time.getString("text");
                                    JSONObject departure_time = (JSONObject) leg.get("departure_time");
                                    String departure_time_text = departure_time.getString("text");
                                    JSONObject distance = (JSONObject) leg.get("distance");
                                    String distance_text = distance.getString("text");
                                    JSONObject duration = (JSONObject) leg.get("duration");
                                    String duration_text = duration.getString("text");
                                    String end_address = leg.getString("end_address");
                                    String start_address = leg.getString("start_address");
                                    dirStrBuilder.append("Departure Time: ").append(departure_time_text).append("\n")
                                            .append("Arrival Time: ").append(arrival_time_text).append("\n")
                                            .append("Start Address: ").append(start_address).append("\n")
                                            .append("End Address: ").append(end_address).append("\n")
                                            .append("Distance: ").append(distance_text).append("\n")
                                            .append("Duration: ").append(duration_text).append("\n");
                                    JSONArray steps = leg.getJSONArray("steps");
                                    dirStrBuilder.append("\nInstructions:\n");
                                    for (int i = 0; i < steps.length(); i++) {
                                        JSONObject step = (JSONObject) steps.get(i);
                                        dirStrBuilder.append("\nStep ").append(String.valueOf(i + 1)).append(":\n")
                                                .append(Html.fromHtml(step.getString("html_instructions"))).append("\n");
                                        if (!step.isNull("steps")) {
                                            JSONArray substeps = step.getJSONArray("steps");
                                            for (int j = 0; j < substeps.length(); j++) {
                                                //JSON substep = substeps.get(j);
                                                JSONObject substep = (JSONObject) substeps.get(j);
                                                dirStrBuilder.append("Sub-Step ").append(String.valueOf(j + 1)).append(":\n")
                                                        .append(Html.fromHtml(substep.getString("html_instructions"))).append("\n");
                                            }
                                        }
                                    }
                                    directions = dirStrBuilder.toString();
                                    break;
                                }
                                case "NOT_FOUND": {
                                    directions = status;
                                    break;
                                }
                                case "ZERO_RESULTS": {
                                    directions = status;
                                    break;
                                }
                                case "MAX_WAYPOINTS_EXCEEDED": {
                                    directions = status;
                                    break;
                                }
                                case "MAX_ROUTE_LENGTH_EXCEEDED": {
                                    directions = status;
                                    break;
                                }
                                case "INVALID_REQUEST": {
                                    directions = status;
                                    break;
                                }
                                case "OVER_DAILY_LIMIT": {
                                    directions = status;
                                    break;
                                }
                                case "OVER_QUERY_LIMIT": {
                                    directions = status;
                                    break;
                                }
                                case "REQUEST_DENIED": {
                                    directions = status;
                                    break;
                                }
                                case "UNKNOWN_ERROR": {
                                    directions = status;
                                    break;
                                }
                                default: {
                                    directions = status;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        numberOfRequestsToMake--;
                        // Dismiss the progressdialog
                        progressDialog.dismiss();
                        queryTextView.setText(directions);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
                // Dismiss the progressdialog
                progressDialog.dismiss();
            }
        });

        jsonObjectRequest.setTag(TAG);

        // Add the request to the RequestQueue.
        mRequestQueue.add(jsonObjectRequest);
    }

    public void prepareMessage(View view) {
        if (isServiceRunning()) {
            if (numberOfRequestsToMake == 0) {
                sendBroadcastMessage();
                //finish();
                FLAG = 1;
                finishAndRemoveTask();
            } else {
                FancyToast.makeText(getApplicationContext(),
                        "Directions not fetched",
                        FancyToast.LENGTH_SHORT, FancyToast.WARNING, false)
                        .show();
            }
        } else {
            Intent serviceIntent = new Intent(this, HelperService.class);
            startService(serviceIntent);
            FancyToast.makeText(getApplicationContext(),
                    "Service was stopped\nNow it's turned ON\nYou can send the answer now",
                    FancyToast.LENGTH_LONG, FancyToast.INFO, false)
                    .show();
        }
    }

    private void sendBroadcastMessage() {
        if (directions != null) {
            Intent intent = new Intent(ACTION_HELP_BROADCAST);
            intent.putExtra(EXTRA_SENDER_ID, sender_id);
            intent.putExtra(EXTRA_SOURCE, source);
            intent.putExtra(EXTRA_DESTINATION, destination);
            intent.putExtra(EXTRA_HELP_ANSWER, directions);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            FancyToast.makeText(getApplicationContext(),
                    "No direction fetched. Can't send help",
                    FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                    .show();
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (HelperService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (FLAG == 0) {
            finishAndRemoveTask();
        }
    }
}
