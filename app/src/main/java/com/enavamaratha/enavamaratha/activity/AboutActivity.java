
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

public class AboutActivity extends BaseActivity
{
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



