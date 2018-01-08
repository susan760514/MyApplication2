package com.example.user123.myapplication;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    WebView wb;
    String tag = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wb = (WebView) findViewById(R.id.wb);

        wb.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.d(tag, "shouldInterceptRequest");
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(tag, "onPageStarted");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.d(tag, "shouldOverrideUrlLoading");
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                Log.d(tag, "onReceivedSslError");
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Log.d(tag, "onReceivedHttpError");
                Log.d(tag, "" + errorResponse.getStatusCode());
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.d(tag, "onReceivedError1 :　" + errorCode);
                Log.d(tag, "onReceivedError1 :　" + description);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.d(tag, "onReceivedError2");
//                Log.d(tag, error.getDescription() +"");
            }
        });

        wb.loadUrl("http://12345677.com.tw");
//        wb.loadUrl("http://eventstest.ntpc.gov.tw/app/garbage/truckmap.jsp?region=%E8%87%BA%E6%9D%B1%E7%B8%A3&locality=%E9%97%9C%E5%B1%B1%E9%8E%AE&mregion=%E8%87%BA%E6%9D%B1%E7%B8%A3&mlocality=%E9%97%9C%E5%B1%B1%E9%8E%AE");
//        wb.loadUrl("https://www.google.com.tw/");
    }
}
