package com.example.soundrecording;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class BgAudioRecordingService extends Service {

    private static final String TAG = "AudioRecordingService";
    private String fileName;
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