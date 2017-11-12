package com.enavamaratha.enavamaratha.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class PollActivity extends AppCompatActivity {


    WebView web;
    public Context context;
    private AdView sAdview,sAdview_right;
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

        // get internt extra when notificatin arrives as a web url
        String notification = getIntent().getStringExtra("Notification");

        System.out.println("Poll Intenet vValue"+simple);
        // for poll
        String url="http://web1.abmra.in/pollform.html";

        //String url="https://www.journal-theme.com/10/";
        // for quiz form
        String url1=" http://web1.abmra.in/quizform.html";
        // for games
     //  String url2="http://web1.abmra.in/games.html";
        //String url2="file:///android_asset/NavaMaratha/games.html";

       // String url2="https://www.smashingmagazine.com/wp-content/uploads/2012/07/final.html";

        //String url2="http://circle-game.sysach.com/";

       // String url2="http://gopherwoodstudios.com/sandtrap/sand-trap.htm";

        String url2="https://0.s3.envato.com/files/93711313/index.html";

        String url3="http://avirtum.com/demo/junglematch/";

        String urll="http://www.baptistebrunet.com/games/fruit_salad/";



      /*  // smal ads on left side
        RelativeLayout smallad=(RelativeLayout)findViewById(R.id.smallad_left);
        sAdview = new AdView(getApplicationContext());
        AdSize smallsize = new AdSize(50,50);
        sAdview.setAdSize(smallsize);
        sAdview.setAdUnitId("ca-app-pub-4094279933655114/3492658587");
        smallad.addView(sAdview);
        AdRequest adre=new AdRequest.Builder().build();
        sAdview.loadAd(adre);

        // small ads on right side
        RelativeLayout smallad_right=(RelativeLayout)findViewById(R.id.smallad_right);
        sAdview_right = new AdView(getApplicationContext());
        AdSize smalls = new AdSize(50,50);
        sAdview_right.setAdSize(smalls);
        sAdview_right.setAdUnitId("ca-app-pub-4094279933655114/2015925381");
        smallad_right.addView(sAdview_right);
        AdRequest adreq=new AdRequest.Builder().build();
        sAdview_right.loadAd(adreq);*/


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
                web.loadUrl(url);
            } else if (simple.equals("quiz")) {
                getSupportActionBar().setTitle(R.string.question);
                web.loadUrl(url1);
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
            showAlertDialog(PollActivity.this, "No Internet Connection", "You don't have internet connection..Please Try Again Later. ", false);
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
       /* AlertDialog alertDialog = new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon((status) ? R.drawable.ic_error_outline : R.drawable.ic_error_outline);


        // Showing Alert Message
        alertDialog.show();*/
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

       /* if( sAdview!= null ||  sAdview_right!= null)
        {

            sAdview.resume();
            sAdview_right.resume();
        }*/

        //Show the AdView if the data connection is available

        if(cd.isConnectingToInternet(getApplicationContext()))
        {

           /* sAdview.setVisibility(View.VISIBLE);
            sAdview_right.setVisibility(View.VISIBLE);*/


        }


      /*  sAdview.resume();
        sAdview_right.resume();
*/
    }

    @Override
    protected void onPause() {


       /* if(sAdview!=null ||  sAdview_right!=null)
        {

            sAdview.pause();
            sAdview_right.pause();
        }
*/

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {

       /* if( sAdview!=null ||  sAdview_right!=null)
        {

            sAdview.destroy();
            sAdview_right.destroy();
        }

*/

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


