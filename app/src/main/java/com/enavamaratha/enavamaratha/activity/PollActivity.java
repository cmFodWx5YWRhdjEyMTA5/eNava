package com.enavamaratha.enavamaratha.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;

import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.POLL_ADDRESS;
import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.QUIZ_ADDRESS;


public class PollActivity extends AppCompatActivity {


    WebView web;
    public Context context;

    ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        web = (WebView) findViewById(R.id.pollview);
        context = getApplicationContext();


        String simple=getIntent().getStringExtra("poll");

        // get intent extra when notification arrives as a web url
        String notification = getIntent().getStringExtra("Notification");


        String urll="http://www.baptistebrunet.com/games/fruit_salad/";




        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web.getSettings().setLoadsImagesAutomatically(true);
        web.getSettings().setSupportZoom(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        web.setScrollbarFadingEnabled(true);
        web.getSettings().setPluginState(WebSettings.PluginState.ON);
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        web.getSettings().getDomStorageEnabled();


        // if internet avaliable then goto paritucular link
        if(cd.isConnectingToInternet(context)) {
            if (simple.equals("poll")) {
                getSupportActionBar().setTitle(R.string.poll);
                web.loadUrl(POLL_ADDRESS);
            } else if (simple.equals("quiz")) {
                getSupportActionBar().setTitle(R.string.question);
                web.loadUrl(QUIZ_ADDRESS);
            } else if (simple.equals("games")) {
                getSupportActionBar().setTitle(R.string.game);
                web.loadUrl(urll);

                // web.loadUrl(" file:///android_asset/NavaMaratha/games.html");
            } else if (simple.contains("Web")) {
                getSupportActionBar().setTitle(R.string.app_name);
                web.loadUrl(notification);
            }


            // web.loadUrl(url);
            web.setWebViewClient(new MyBrowser());
        }

        // else show no internet connection
        else
        {
            showAlertDialog(PollActivity.this, getResources().getString(R.string.no_internet), getResources().getString(R.string.no_internet_msg), false);
        }

    }
    public void showAlertDialog(final Context context, String title, String message, Boolean status)
    {
        // material dialog
          AlertDialog.Builder builder = new AlertDialog.Builder(PollActivity.this);
            builder.setTitle(title);
            builder.setMessage(message);

            String positiveText = getString(android.R.string.ok);

           AlertDialog dialog = builder.create();
            // display dialog
            dialog.show();

    }
        public class MyBrowser extends WebViewClient
        {

            public boolean shouldOverrideUrlLoading(WebView web, String url)
            {

                web.loadUrl(url);

                return false;

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.scrollTo(0,150);
            }
        }


        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event)
        { //if back key is pressed
            if((keyCode == KeyEvent.KEYCODE_BACK)&& web.canGoBack())
            {
                web.goBack();
                return true;
            }

            return super.onKeyDown(keyCode, event);
        }


    @Override
    protected void onResume() {

        super.onResume();


    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        //   return true;
        // }
        switch (id)
        {
            case R.id.menu_homee:
                Intent intee = new Intent(PollActivity.this,HomeActivity.class);
                intee.putExtra("home","home");
                startActivity(intee);
                return true;


            case android.R.id.home:
                Intent intt = new Intent(PollActivity.this,HomeActivity.class);
                intt.putExtra("home","home");
                startActivity(intt);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intt = new Intent(PollActivity.this,HomeActivity.class);
        intt.putExtra("home","home");
        startActivity(intt);
        finish();

    }
    }


