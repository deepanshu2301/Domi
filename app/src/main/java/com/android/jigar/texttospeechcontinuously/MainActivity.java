package com.android.jigar.texttospeechcontinuously;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ImageButton speak, stop , webcall;
    static EditText textView;
    private SpeechRecognizer sr;
    private static final String TAG = "MyActivity";
    ProgressDialog dialog;
    int code;
    private Messenger mServiceMessenger;
    boolean isEndOfSpeech = false;
    boolean serviceconneted;

    static final Integer LOCATION = 0x1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speak = findViewById(R.id.speak);
        stop = findViewById(R.id.stop);
        webcall = findViewById(R.id.websearch);
        textView = findViewById(R.id.write);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyService.class);
                stopService(i);
                Toast.makeText(MainActivity.this, "stop speaking", Toast.LENGTH_SHORT).show();
            }
        });
        sr = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        sr.setRecognitionListener(new Listner());

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    askForPermission(Manifest.permission.RECORD_AUDIO, LOCATION);
                }
                Intent i = new Intent(MainActivity.this, MyService.class);
                bindService(i, connection, code);
                startService(i);
                Toast.makeText(MainActivity.this, "Start Speaking", Toast.LENGTH_SHORT).show();


            }
        });

        final WebView myWebView = (WebView) findViewById(R.id.webviewact);


        webcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myWebView.loadUrl("http://www.google.com");
            }
        });
    }

    class Listner implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d("Speech", "ReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d("Speech", "beginSpeech");

        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d("Speech", "onrms");

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d("Speech", "onbuffer");

        }

        @Override
        public void onEndOfSpeech() {
            isEndOfSpeech = true;

        }

        @Override
        public void onError(int error) {
            Log.d(TAG, "error " + error);
            if (!isEndOfSpeech) {
                return;
            }
            Toast.makeText(MainActivity.this, "Try agine", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);
            textView.setText(word);
            dialog.dismiss();


        }

        @Override
        public void onPartialResults(Bundle partialResults) {

            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);
            textView.setText(word);

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }


    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            }

        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }



    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d("service", "connected");

            mServiceMessenger = new Messenger(service);
            Message msg = new Message();
            msg.what = MyService.MSG_RECOGNIZER_START_LISTENING;
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            serviceconneted = false;
            Log.d("service", "disconnetd");
        }
    };
}

    
