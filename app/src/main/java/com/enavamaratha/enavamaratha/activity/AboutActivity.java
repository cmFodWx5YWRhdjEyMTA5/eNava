
package  com.enavamaratha.enavamaratha.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.provider.FeedData;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.enavamaratha.enavamaratha.utils.PrefUtils;
import com.enavamaratha.enavamaratha.utils.UiUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AboutActivity extends BaseActivity
{
    private AdView sAdview,sAdview_right;
    ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.about);

        WebView webView = (WebView) findViewById(R.id.abtWebview );
        webView.setVerticalScrollBarEnabled(true);
        webView.loadUrl("file:///android_asset/NavaMaratha/Aboutuu.html");
        webView.getSettings().setJavaScriptEnabled(true);


      /*  // Small Advertise
        RelativeLayout smallad=(RelativeLayout)findViewById(R.id.smallad_left);
        sAdview = new AdView(getApplicationContext());
        AdSize smallsize = new AdSize(50,50);
        sAdview.setAdSize(smallsize);
        sAdview.setAdUnitId("ca-app-pub-6878344570840014/7674258084");
        smallad.addView(sAdview);
        AdRequest adre=new AdRequest.Builder().build();
        sAdview.loadAd(adre);

        // small ads on right side
        RelativeLayout smallad_right=(RelativeLayout)findViewById(R.id.smallad_right);
        sAdview_right = new AdView(getApplicationContext());
        AdSize smalls = new AdSize(50,50);
        sAdview_right.setAdSize(smalls);
        sAdview_right.setAdUnitId("ca-app-pub-6878344570840014/2794903282");
        smallad_right.addView(sAdview_right);
        AdRequest adreq=new AdRequest.Builder().build();
        sAdview_right.loadAd(adreq);*/
    }
    @Override
    protected void onResume() {

        super.onResume();

        /*if( sAdview!= null ||  sAdview_right!= null)
        {

            sAdview.resume();
            sAdview_right.resume();
        }

        //Show the AdView if the data connection is available

        if(cd.isConnectingToInternet(getApplicationContext()))
        {

            sAdview.setVisibility(View.VISIBLE);
            sAdview_right.setVisibility(View.VISIBLE);


        }


        sAdview.resume();
        sAdview_right.resume();*/

    }

    @Override
    protected void onPause() {

/*

        if(sAdview!=null ||  sAdview_right!=null)
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
        switch (id) {
            case R.id.menu_homee:
                Intent intee = new Intent(AboutActivity.this,HomeActivity.class);
                intee.putExtra("home","home");
                startActivity(intee);
                return true;


            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    }



