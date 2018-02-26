package com.example.user123.myapplication;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mTopCoverView;
    WebView wb;
    String tag = "WebViewMainActivity";

    private boolean shouldInvisible = false;
    private boolean hasErrorCode = false;

    private static final int DO_TASK1 = 0;

    private String htmlCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wb = (WebView) findViewById(R.id.wb);
        mTopCoverView = (RelativeLayout) findViewById(R.id.top_cover_view);

        setWebViewSetting();

        wb.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                Log.d(tag, "onReceivedSslError : " + error.toString());
            }

            //onReceivedError舊版本
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

                Log.d(tag, "onReceivedError1 :　" + errorCode);
                Log.d(tag, "onReceivedError1 :　" + description);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//新版本也可能被調用,所以加上判斷,防止重複進入
                    return;
                } else {
                    hasErrorCode = true;
                    checkErrorCode(1, errorCode, description);
                }
            }

            //onReceivedError新版本,只在Android6.0以上調用(含6.0)
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                Log.d(tag, "onReceivedError2 :　" + error.getErrorCode());
                Log.d(tag, "onReceivedError2 :　" + error.getDescription());

                if (request.isForMainFrame()) { //是發出的request否為Webview
                    wb.loadUrl("");
                    hasErrorCode = true;
                    checkErrorCode(2, error.getErrorCode(), error.getDescription().toString());
                }
            }

            //只在Android6.0以上調用(含6.0)
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);

                Log.d(tag, "onReceivedHttpError");
                Log.d(tag, "" + errorResponse.getStatusCode());

                if (request.isForMainFrame()) {
                    hasErrorCode = true;
                    needToInvisibleWebView(3, errorResponse.getStatusCode() + "");
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(tag, "onPageStarted");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !hasErrorCode) { //沒有 error code跑這段
                    //利用javasript抓取網頁原始碼
//                    view.loadUrl("javascript:window.local_obj.showSource('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                    //向網址取的連線response 利用Okhttp完成
                } else { //有error code跑這段
                    if (!shouldInvisible) {
                        Log.d(tag,"有erroc code 但不影響");
                        mTopCoverView.setVisibility(View.GONE);
                    } else {
                        Log.d(tag,"有erroc code 已經跳alert了 不放行");
                    }
                }
            }
        });

//        wb.setWebChromeClient(new WebChromeClient(){
//            @Override
//            public void onReceivedTitle(WebView view, String title) {
//                if(!TextUtils.isEmpty(title)&&title.toLowerCase().contains("網頁發生錯誤")){
//                    Log.d(tag, "網頁發生錯誤");
//                }
//            }
//        });

        //停車繳費 測試cache miss
//        String postData = "is_member=1" + "&uid=AAA3631" + "&status=rbvYaGVbVWw_L6jApKkDzF7xGQOpJTziSjr4rmUvNzo";
//        wb.postUrl("https://www.mpayment.ntpc.gov.tw/NTCMPAYMWeb/jsp/MPAYM0002.action", postData.getBytes());
//        wb.loadUrl("https://www.mpayment.ntpc.gov.tw/NTCMPAYMWeb/jsp/MPAYM0002.action?id=AAA3631");

        //測試404 with error page
        wb.loadUrl("https://www.mpayment.ntpc.gov.tw/NTCMPAYMWeb/Indeffx.action");
        //測試404-2
//        wb.loadUrl("http://eventstest.ntpc.gov.tw/app/json/adddd.jsp?");

        //without error page
//        wb.loadUrl("http://172.18.20.149:8080/NTCMPAYMWeb/jsp/MPAYM0001.action");
//        wb.loadUrl("http://3252355.com.tw");

        //市醫掛號
//        wb.loadUrl("http://eventstest.ntpc.gov.tw/app/hospital/myschedule.jsp?identifier=AAA3631");
    }

    private void setWebViewSetting() {
        wb.getSettings().setJavaScriptEnabled(true);
        wb.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");

        //停車繳費須加上這段 不然會出現原生的error page fix ERR_CACHE_MISS 在瀏覽器看也會
        wb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//LOAD_CACHE_ELSE_NETWORK-無論是否有網路，只要本地有緩存，都使用缓存。本地没有缓存時才從網路上獲取。
    }

    //判斷error code
    private void checkErrorCode(int type, int errorCode, String description) {
        if (errorCode == -1) { //net::ERR_CACHE_MISS
            needToInvisibleWebView(type, errorCode + "\n" + description);
        } else if (errorCode == -2) { //找不到網址 net::ERR_NAME_NOT_RESOLVED
            needToInvisibleWebView(type, errorCode + "\n" + description);
        } else if (errorCode == -6) { //net::ERR_CONNECTION_REFUSED
            needToInvisibleWebView(type, errorCode + "\n" + description);
        } else if (errorCode == -11) { //無法建立安全連線
            needToInvisibleWebView(type, errorCode + "\n" + description);
        } else {

        }
    }

    private void needToInvisibleWebView(int type, String description) {
        Log.d(tag, type + ". 顯示錯誤提示不讓error page顯示");
        shouldInvisible = true;
        showAlert("系統維護中請稍後再試", description);
    }

    private void showAlert(String title, String msg) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(msg)//設定顯示的文字
                .setPositiveButton("確定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })//設定結束的子視窗
                .show();//呈現對話視窗
    }

    //利用javascrip印出網頁html碼
    public final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showSource(String html) {
            htmlCode = html;
            if ((htmlCode.contains("status_code:") && htmlCode.contains("404"))) {
                shouldInvisible = true;
                needToInvisibleWebView(4, "網頁錯誤"); //無error code , html解析後顯示提醒alert
//                Log.d(tag, "===============HTML CODE===============" + htmlCode);
            } else {
                Log.d(tag, "無error code , html解析後放行");
                shouldInvisible = false;
                mTopCoverView.setVisibility(View.GONE);//TODO 要另外建執行序 execption: Only the original thread that created a view hierarchy can touch its views
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (wb.canGoBack()) {
                        shouldInvisible = false;
                        wb.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
