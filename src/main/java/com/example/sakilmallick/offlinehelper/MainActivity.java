package com.example.sakilmallick.offlinehelper;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String
            ACTION_QUERY_BROADCAST = MainActivity.class.getName() + "QueryBroadcast",
            EXTRA_SOURCE = "extra_source",
            EXTRA_DESTINATION = "extra_destination",
            EXTRA_TRAVEL_MODE = "extra_travel_mode";
    String travel_mode;

    // UI elements
    EditText sourceEditText;
    EditText destinationEditText;
    TextView answersTextView;
    Button travelModeChoiceButton;

    String[] listTravelModes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listTravelModes = getResources().getStringArray(R.array.travel_mode);
        // Default travel mode is driving
        travel_mode = listTravelModes[0];

        // Layout initialisation
        travelModeChoiceButton = findViewById(R.id.travelModeChoiceButton);
        sourceEditText = findViewById(R.id.sourceEditText);
        destinationEditText = findViewById(R.id.destinationEditText);
        answersTextView = findViewById(R.id.answersTextView);

        // Answers textview made scrollable
        //answersTextView.setMovementMethod(new ScrollingMovementMethod());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Runtime permission handling with Dexter
            Dexter.withActivity(this)
                    .withPermissions(
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.FOREGROUND_SERVICE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            // check if all permissions are granted
                            if (report.areAllPermissionsGranted()) {
                                //Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                                //FancyToast.makeText(getApplicationContext(), "All permissions are granted!", FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,false).show();
                                Log.i(Constants.TAG, "All permissions are granted!");
                                // Receive answers sent from HelperService using Broadcast Manager
                                // And display them
                                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                                        new BroadcastReceiver() {
                                            @Override
                                            public void onReceive(Context context, Intent intent) {
                                                String directions = intent.getStringExtra(HelperService.EXTRA_ANSWER);
                                                answersTextView.setText(directions);
                                            }
                                        }, new IntentFilter(HelperService.ACTION_ANSWER_BROADCAST)
                                );

                                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                                        new BroadcastReceiver() {
                                            @Override
                                            public void onReceive(Context context, Intent intent) {
                                                Boolean signal = intent.getBooleanExtra(HelperService.EXTRA_SIGNAL, false);
                                                if (signal) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                    builder.setTitle("Internet required to start service");
                                                    builder.setMessage("Internet connection is required for the first time the service starts for user verification." +
                                                            " Connect to the Internet and then start the service again.");
                                                    builder.setNeutralButton("OK", (dialog, which) -> {
                                                        dialog.cancel();
                                                    });
                                                    builder.show();
                                                }
                                            }
                                        }, new IntentFilter(HelperService.ACTION_SIGNAL_BROADCAST)
                                );
                            }

                            // check for permanent denial of any permission
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                FancyToast.makeText(getApplicationContext(),
                                        "Please allow permissions to use the up",
                                        FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                                        .show();
                                // show alert dialog navigating to Settings
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).
                    withErrorListener(error -> Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show())
                    .onSameThread()
                    .check();
        } else {
            // Runtime permission handling with Dexter
            Dexter.withActivity(this)
                    .withPermissions(
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            // check if all permissions are granted
                            if (report.areAllPermissionsGranted()) {
                                //Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                                //FancyToast.makeText(getApplicationContext(), "All permissions are granted!", FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,false).show();
                                Log.i(Constants.TAG, "All permissions are granted!");
                                // Receive answers sent from HelperService using Broadcast Manager
                                // And display them
                                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                                        new BroadcastReceiver() {
                                            @Override
                                            public void onReceive(Context context, Intent intent) {
                                                String cur_answer = intent.getStringExtra(HelperService.EXTRA_ANSWER);
                                                answersTextView.append(cur_answer + "\n");
                                            }
                                        }, new IntentFilter(HelperService.ACTION_ANSWER_BROADCAST)
                                );
                                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                                        new BroadcastReceiver() {
                                            @Override
                                            public void onReceive(Context context, Intent intent) {
                                                Boolean signal = intent.getBooleanExtra(HelperService.EXTRA_SIGNAL, false);
                                                if (signal) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                    builder.setTitle("Internet required to start service");
                                                    builder.setMessage("Internet connection is required for the first time the service starts for user verification." +
                                                            " Connect to the Internet and then start the service again.");
                                                    builder.setNeutralButton("OK", (dialog, which) -> {
                                                        dialog.cancel();
                                                    });
                                                    builder.show();
                                                }
                                            }
                                        }, new IntentFilter(HelperService.ACTION_SIGNAL_BROADCAST)
                                );
                            }

                            // check for permanent denial of any permission
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                FancyToast.makeText(getApplicationContext(),
                                        "Please allow permissions to use the up",
                                        FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                                        .show();
                                // show alert dialog navigating to Settings
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).
                    withErrorListener(error -> Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show())
                    .onSameThread()
                    .check();
        }
    }

    // Function to open app info when at least one permission is denied
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    public void textQueryActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), TextQueryActivity.class);
        startActivity(intent);
    }

    public void startService(View view) {
        if (!isServiceRunning()) {
            Intent serviceIntent = new Intent(this, HelperService.class);
            startService(serviceIntent);
        } else {
            FancyToast.makeText(getApplicationContext(),
                    "Service already running",
                    FancyToast.LENGTH_SHORT, FancyToast.WARNING, false)
                    .show();
        }
    }

    public void showTravelModes(View view) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Choose a travel mode");
        mBuilder.setSingleChoiceItems(listTravelModes, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                travelModeChoiceButton.setText("Travel Mode: " + listTravelModes[i]);
                travel_mode = listTravelModes[i];
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    public void prepareMessage(View view) {
        if (!(TextUtils.isEmpty(sourceEditText.getText()))
                && !(TextUtils.isEmpty(destinationEditText.getText()))) {
            if (isServiceRunning()) {
                String source = sourceEditText.getText().toString();
                String destination = destinationEditText.getText().toString();
                sendBroadcastMessage(source, destination);
            } else {
                FancyToast.makeText(getApplicationContext(),
                        "Start the service first",
                        FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                        .show();
            }
        } else {
            FancyToast.makeText(getApplicationContext(),
                    "Enter source and destination both",
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

    private void sendBroadcastMessage(String source, String destination) {
        if (source != null && destination != null) {
            Intent intent = new Intent(ACTION_QUERY_BROADCAST);
            intent.putExtra(EXTRA_SOURCE, source);
            intent.putExtra(EXTRA_DESTINATION, destination);
            intent.putExtra(EXTRA_TRAVEL_MODE, travel_mode);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
