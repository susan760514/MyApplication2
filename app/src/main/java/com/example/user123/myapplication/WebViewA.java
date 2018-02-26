package com.example.user123.myapplication;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URL;

/* WebView 遇到http error時的優化
附註
1.重要 : isForMainFrame 才是正確的error code (有的網頁正常顯示 但會回傳404)(例:市醫掛號)
2.會先得到errorcode才跑finished
3.started只會跑第一次, finished才能得到每一次動作的url(例返回 下一頁)
 */

public class WebViewA extends AppCompatActivity {

    private RelativeLayout mTopCoverView;
    WebView wb;
    String tag = "WebViewA";

    private boolean hasErrorCode = false;
    private boolean isSuccess = false;
    private boolean shouldInVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wb = (WebView) findViewById(R.id.wb);
        mTopCoverView = (RelativeLayout) findViewById(R.id.top_cover_view);

        setWebViewSetting();

        wb.setWebViewClient(new WebViewClient() {
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
                    checkErrorCode(errorCode);
                }
            }

            //onReceivedError新版本
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                Log.d(tag, "onReceivedError2 :　" + error.getErrorCode());
                Log.d(tag, "onReceivedError2 :　" + error.getDescription());

                if (request.isForMainFrame()) {
                    hasErrorCode = true;
                    checkErrorCode(error.getErrorCode());
                } else {
                    //TODO
                }
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);

                //重要 : isForMainFrame 才是正確的error code (有的網頁正常顯示 但會回傳404)
                if (request.isForMainFrame()) {
                    hasErrorCode = true;
                    showAlert(errorResponse.getStatusCode() + " error");
                }
            }

            //started只會跑一次
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            //剛進入結束時, 返回頁面結束時都會跑
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !hasErrorCode) {
                    catchHttpConnection(url);
                } else {
                    if (!shouldInVisible) {
                        mTopCoverView.setVisibility(View.VISIBLE);
                    } else {
                        mTopCoverView.setVisibility(View.GONE);
                    }
                }
            }
        });

        //停車繳費 測試cache miss
//        String postData = "is_member=1" + "&uid=AAA3631" + "&status=rbvYaGVbVWw_L6jApKkDzF7xGQOpJTziSjr4rmUvNzo";
//        wb.postUrl("https://www.mpayment.ntpc.gov.tw/NTCMPAYMWeb/jsp/MPAYM0002.action", postData.getBytes());
//        wb.loadUrl("https://www.mpayment.ntpc.gov.tw/NTCMPAYMWeb/jsp/MPAYM0002.action?id=AAA3631");

        //測試404 with error page
        wb.loadUrl("https://www.mpayment.ntpc.gov.tw/NTCMPAYMWeb/Indeffx.action");
        //測試404-2 with error page
//        wb.loadUrl("http://eventstest.ntpc.gov.tw/app/json/adddd.jsp?");

        //without error page
//        wb.loadUrl("http://172.18.20.149:8080/NTCMPAYMWeb/jsp/MPAYM0001.action"); //停車繳費測試機
//        wb.loadUrl("http://3252355.com.tw");

        //市醫掛號
//        wb.loadUrl("http://eventstest.ntpc.gov.tw/app/hospital/myschedule.jsp?identifier=AAA3631");
    }

    //設定webview需要用到的參數
    private void setWebViewSetting() {
        //停車繳費須加上這段 不然會出現原生的error page fix ERR_CACHE_MISS 在瀏覽器看也會
        wb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//LOAD_CACHE_ELSE_NETWORK-無論是否有網路，只要本地有緩存，都使用缓存。本地没有缓存時才從網路上獲取。
    }

    //判斷error code
    private void checkErrorCode(int errorCode) {
        //-1 net::ERR_CACHE_MISS
        //-2 net::ERR_NAME_NOT_RESOLVED 找不到網址
        //-6 net::ERR_CONNECTION_REFUSED
        //-11 無法建立安全連線
        if (errorCode == -1 || errorCode == -2 || errorCode == -6 || errorCode == -11) {
            showAlert(errorCode + " error");
        } else {
            //TODO
        }
    }

    //頁面錯誤 提醒使用者離開頁面
    private void showAlert(String msg) {
        shouldInVisible = true;
        new AlertDialog.Builder(WebViewA.this)
                .setMessage(msg)//設定顯示的文字
                .setPositiveButton("確定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })//設定結束的子視窗
                .show();//呈現對話視窗
    }

    //6.0 以下抓不到error code 需判斷 http response
    private void catchHttpConnection(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = client.newCall(request).execute();
                    isSuccess = response.isSuccessful();

                    Log.d(tag, "catchHttpConnection : " +path + ", success : " + isSuccess);

                    if (isSuccess) {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) { //解析網址錯誤時
                    Log.d(tag, "e : " + e.toString());
                    Message msg = Message.obtain();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    //判斷 http response 是否顯示頁面
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                mTopCoverView.setVisibility(View.GONE);
            } else {
                mTopCoverView.setVisibility(View.VISIBLE);
                showAlert("error");
            }
        }
    };

    //返回按鈕
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (wb.canGoBack()) {
                        shouldInVisible = false;
                        wb.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //destroy時清掉cache
    @Override
    public void onDestroy() {
        super.onDestroy();
        wb.clearCache(true); // 清除暫存 cache !!
    }
}
