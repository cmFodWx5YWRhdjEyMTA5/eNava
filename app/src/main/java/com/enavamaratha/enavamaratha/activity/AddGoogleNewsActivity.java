package  com.enavamaratha.enavamaratha.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import  com.enavamaratha.enavamaratha.R;
import  com.enavamaratha.enavamaratha.provider.FeedDataContentProvider;
import  com.enavamaratha.enavamaratha.utils.UiUtils;

import java.util.Locale;

public class AddGoogleNewsActivity extends BaseActivity {

    private static final int[] TOPIC_NAME = new int[]{R.string.google_news_top_stories, R.string.google_news_world, R.string.google_news_business,
            R.string.google_news_technology, R.string.google_news_entertainment, R.string.google_news_sports, R.string.google_news_science, R.string.google_news_health};

    private static final String[] TOPIC_CODES = new String[]{null, "w", "b", "t", "e", "s", "snc", "m"};

    private static final int[] CB_IDS = new int[]{R.id.cb_top_stories, R.id.cb_world, R.id.cb_business, R.id.cb_technology, R.id.cb_entertainment,
            R.id.cb_sports, R.id.cb_science, R.id.cb_health};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.activity_add_google_news);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    public void onClickOk(View view) {
        for (int topic = 0; topic < TOPIC_NAME.length; topic++) {
            if (((CheckBox) findViewById(CB_IDS[topic])).isChecked()) {
                String url = "http://news.google.com/news?hl=" + Locale.getDefault().getLanguage() + "&output=rss";
                if (TOPIC_CODES[topic] != null) {
                    url += "&topic=" + TOPIC_CODES[topic];
                }
                FeedDataContentProvider.addFeed(this, url, getString(TOPIC_NAME[topic]), true);
            }
        }

        setResult(RESULT_OK);
        finish();
    }

    public void onClickCancel(View view) {
        finish();
    }
}

