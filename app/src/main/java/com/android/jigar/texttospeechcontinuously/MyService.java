package com.android.jigar.texttospeechcontinuously;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MyService extends Service {

    //Use audio manageer services
    static protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    //To turn screen On uitll lock phone
    PowerManager.WakeLock wakeLock;
    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    static boolean mIsStreamSolo;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    static int identify = 0, result = 0;
    String Currentdata = null, newcurrent = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi_IN");
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

        Log.d("service", "oncreate");
    }

    protected static class IncomingHandler extends Handler {
        private WeakReference<MyService> mtarget;

        public IncomingHandler(MyService target) {
            mtarget = new WeakReference<MyService>(target);
        }


        @Override
        public void handleMessage(Message msg) {
            final MyService target = mtarget.get();

            switch (msg.what) {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // turn off beep sound
                        if (!mIsStreamSolo) {
                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                            mIsStreamSolo = true;
                        }
                    }
                    if (!target.mIsListening) {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                        //Log.d(TAG, "message start listening"); //$NON-NLS-1$
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    if (mIsStreamSolo) {
                        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        mIsStreamSolo = false;
                    }
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    //Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
                    break;
            }
        }
    }

    // Count down timer for Jelly Bean work around
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(2000, 2000) {

        @Override
        public void onTick(long millisUntilFinished) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFinish() {
            mIsCountDownOn = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
            try {
                mServerMessenger.send(message);
                message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                mServerMessenger.send(message);
            } catch (RemoteException e) {

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIsCountDownOn) {
            mNoSpeechCountDown.cancel();
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServerMessenger.getBinder();
    }

    protected class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
            // speech input will be processed, so there is no need for count down anymore
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            //Log.d(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            //Log.d(TAG, "onEndOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onError(int error) {
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            mIsListening = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {

            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

            Log.i("spech", "onPartialResults");

            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);
            if (Currentdata == null) {
                MainActivity.textView.setText("" + word);
            } else {
                MainActivity.textView.setText(Currentdata + " " + word);
                MainActivity.textView.setSelection(MainActivity.textView.getText().length());
            }

            newcurrent = MainActivity.textView.getText().toString();
            identify = 1;
            Log.d("partail", "" + word);
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mIsCountDownOn = true;
                mNoSpeechCountDown.start();
            }
            Log.d("service", "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results) {

            //Log.d(TAG, "onResults"); //$NON-NLS-1$

            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);

            if (result == 0) {
                MainActivity.textView.setText(word);
                Currentdata = MainActivity.textView.getText().toString();
            } else if (result == 1) {
                if (Currentdata != null) {
                    MainActivity.textView.setText(Currentdata + "\n" + word);
                    MainActivity.textView.setSelection(MainActivity.textView.getText().length());
                }
            }
            Currentdata = MainActivity.textView.getText().toString();

            Log.d("service", "" + Currentdata);

            if (mIsListening == true) {
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }
            result = 1;
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

    }
}
