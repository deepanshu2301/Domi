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

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if ("www.example.com".equals(Uri.parse(url).getHost())) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }
}
