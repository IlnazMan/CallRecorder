package com.tech.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordReceiver extends BroadcastReceiver {

    private static MediaRecorder recorder;
    private File audiofile;
    private static boolean recordstarted = false;
    public static boolean wasRinging = false;
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle;
        String state;
        if (intent.getAction().equals(ACTION_IN)) {
            if ((bundle = intent.getExtras()) != null) {
                state = bundle.getString(TelephonyManager.EXTRA_STATE);
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    String inCall = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    wasRinging = true;
                    Log.wtf(context.getPackageName(), "IN : " + inCall);
                } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    if (wasRinging) {
                        Log.wtf(context.getPackageName(), "ANSWERED");
                        File sampleDir = new File(Environment.getExternalStorageDirectory(), "/TestRecordingData1");
                        if (!sampleDir.exists()) {
                            sampleDir.mkdirs();
                        }
                        String fName = new SimpleDateFormat("ddMMyyyy_hhmmss").format(new Date());

                        try {
                            audiofile = File.createTempFile("rec_" + fName, ".amr", sampleDir);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        recorder.setOutputFile(audiofile.getAbsolutePath());
                        try {
                            recorder.prepare();
                            recorder.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        recordstarted = true;
                    }
                } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    wasRinging = false;
                    Log.wtf(context.getPackageName(), "REJECT || DISCO");
                    if (recordstarted)
                        recorder.stop();
                    recordstarted = false;
                }
            }
        } else if (intent.getAction().equals(ACTION_OUT)) {
            if ((intent.getExtras()) != null) {
                String outCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                wasRinging = true;
                Log.wtf(context.getPackageName(), "OUT : " + outCall);
            }
        }
    }
}
