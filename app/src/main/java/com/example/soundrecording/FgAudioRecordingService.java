package com.example.soundrecording;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class FgAudioRecordingService extends Service {
    public static final String CHANNEL_ID = "AudioRecordingServiceChannel";
    private static final String TAG = "AudioRecordingService";
    AudioRecordingManager audioRecordingManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        audioRecordingManager = new AudioRecordingManager(this);
        audioRecordingManager.createAudioRecordingEntry();
        try {

            String input = intent.getStringExtra("inputExtra");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Audio Recording Service")
                    .setContentText(input)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();

            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
            audioRecordingManager.startRecording();
        } catch (IOException e) {
            Log.e(TAG, "Failed to prepare MediaRecorder: " + e.getMessage());
            audioRecordingManager.releaseMediaRecorder();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioRecordingManager.stopRecording();
    }
}