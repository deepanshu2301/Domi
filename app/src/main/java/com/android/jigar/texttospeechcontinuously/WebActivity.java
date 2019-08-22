package com.android.jigar.texttospeechcontinuously;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class WebActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webact);

        String search = getIntent().getStringExtra("Data");
        Toast.makeText(getBaseContext(),""+search.length(),Toast.LENGTH_LONG).show();
        final WebView myWebView = findViewById(R.id.webviewact);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.loadUrl("https://duckduckgo.com/search.html?prefill="+search);
        myWebView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                myWebView.loadUrl("javascript:document.getElementById('search_button_homepage').click()");
            }
        });
    }

}
