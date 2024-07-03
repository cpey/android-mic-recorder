package com.example.soundrecording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import androidx.annotation.NonNull;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AudioRecording";
    private Button fgStartButton;
    private Button fgStopButton;
    private Button bgStartButton;
    private Button bgStopButton;
    private BroadcastReceiver receiver;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupForegroundButtons();
        setupBackgroundButtons();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.AudioRecordingService.RECORDING_COMPLETE".equals(intent.getAction())) {
                    String fileName = intent.getStringExtra("fileName");
                    Log.d(TAG, "Recording complete. File saved at: " + fileName);
                }
            }
        };

        IntentFilter filter = new IntentFilter("com.example.AudioRecordingService.RECORDING_COMPLETE");
        registerReceiver(receiver, filter);

        // Check and request permissions if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            permissionToRecordAccepted = true;
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    private void setupForegroundButtons() {
        fgStartButton = findViewById(R.id.fgStartButton);
        fgStopButton = findViewById(R.id.fgStopButton);

        fgStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionToRecordAccepted) {
                    Log.d(TAG, "Start button pressed");

                    Intent serviceIntent = new Intent(MainActivity.this, FgAudioRecordingService.class);
                    serviceIntent.putExtra("inputExtra", "Recording audio in foreground service");
                    startForegroundService(serviceIntent);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
                }
            }
        });

        fgStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Stop button pressed");
                stopService(new Intent(MainActivity.this, FgAudioRecordingService.class));
            }
        });
    }

    private void setupBackgroundButtons() {
        bgStartButton = findViewById(R.id.bgStartButton);
        bgStopButton = findViewById(R.id.bgStopButton);

        bgStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionToRecordAccepted) {
                    startService(new Intent(MainActivity.this, BgAudioRecordingService.class));
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
                }
            }
        });

        bgStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, BgAudioRecordingService.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Requesting permission 1");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!permissionToRecordAccepted) {
                    finish();
                }
                break;
        }
    }
}