package com.example.soundrecording;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.IOException;

public class AudioRecordingManager {
    private Context context;
    private static final String TAG = "AudioRecordingService";
    MediaRecorder recorder = null;
    Uri fileUri = null;

    public AudioRecordingManager(Context context) {
        this.context = context;
    }

    public void createAudioRecordingEntry() {
        Uri collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        long timeMillis = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "recording" + timeMillis);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_RECORDINGS);
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4");
        values.put(MediaStore.Audio.Media.DATE_TAKEN, timeMillis);
        values.put(MediaStore.Audio.Media.IS_PENDING, 1);

        ContentResolver contentResolver = context.getContentResolver();
        fileUri = contentResolver.insert(collection, values);
    }

    public void startRecording() throws IOException {
        if (fileUri == null) {
            throw new RuntimeException("Missing audio recording");
        }
        if (!checkPermissions()) {
            throw new RuntimeException("Missing permissions");
        }

        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(fileUri, "w");
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(pfd.getFileDescriptor());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
            recorder.start();
            Log.d(TAG, "Recording started");
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed: " + e.getMessage());
            releaseMediaRecorder();
            throw e;
        } catch (RuntimeException e) {
            Log.e(TAG, "start() failed: " + e.getMessage());
            releaseMediaRecorder();
            throw e;
        } finally {
            pfd.close();
        }
    }

    public void stopRecording() {
        String fileName = null;
        if (recorder != null) {
            try {
                recorder.stop();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.IS_PENDING, 0);
                context.getContentResolver().update(fileUri, values, null, null);
                fileName = getRealPathFromURI();
            } catch (RuntimeException stopException) {
                Log.e(TAG, "stop() failed: " + stopException.getMessage());
            } finally {
                releaseMediaRecorder();
            }
            Intent intent = new Intent("com.example.AudioRecordingService.RECORDING_COMPLETE");
            intent.putExtra("fileName", fileName);
            context.sendBroadcast(intent);
        }
    }

    public void releaseMediaRecorder() {
        if (recorder != null){
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    public String getRealPathFromURI() {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(fileUri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    private boolean checkPermissions() {
        int recordPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        return recordPermission == PackageManager.PERMISSION_GRANTED;
    }
}
